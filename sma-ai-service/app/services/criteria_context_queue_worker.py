"""RabbitMQ queue worker for criteria context generation."""

import json
import threading
import time

import pika
from loguru import logger

from app.clients.openai_client import create_json_chat_completion
from app.core.config import settings
from app.prompts.criteria_context import build_criteria_context_prompt


class CriteriaContextQueueWorker:
    """Consumes criteria context requests, calls GPT, publishes results."""

    def __init__(self):
        self._connection = None
        self._channel = None
        self._thread = None
        self._should_stop = False

    # ---- lifecycle ----

    def start(self):
        self._should_stop = False
        self._thread = threading.Thread(target=self._run, daemon=True, name="criteria-context-worker")
        self._thread.start()
        logger.info("Criteria context queue worker started")

    def stop(self):
        self._should_stop = True
        if self._connection and self._connection.is_open:
            try:
                self._connection.close()
            except Exception:
                pass
        if self._thread:
            self._thread.join(timeout=10)
        logger.info("Criteria context queue worker stopped")

    # ---- internal ----

    def _get_connection_params(self):
        return pika.ConnectionParameters(
            host=settings.RABBITMQ_HOST,
            port=settings.RABBITMQ_PORT,
            virtual_host=settings.RABBITMQ_VHOST,
            credentials=pika.PlainCredentials(
                settings.RABBITMQ_USER,
                settings.RABBITMQ_PASSWORD,
            ),
            heartbeat=600,
            blocked_connection_timeout=300,
        )

    def _run(self):
        while not self._should_stop:
            try:
                self._connection = pika.BlockingConnection(self._get_connection_params())
                self._channel = self._connection.channel()

                request_queue = settings.RABBITMQ_CRITERIA_CONTEXT_REQUEST_QUEUE
                self._channel.queue_declare(queue=request_queue, durable=True)
                self._channel.basic_qos(prefetch_count=1)
                self._channel.basic_consume(
                    queue=request_queue,
                    on_message_callback=self._on_message,
                )

                logger.info("Listening on queue '{}'", request_queue)
                self._channel.start_consuming()

            except pika.exceptions.AMQPConnectionError as e:
                if self._should_stop:
                    break
                logger.warning("RabbitMQ connection lost: {}. Reconnecting in {}s...",
                               e, settings.RABBITMQ_RECONNECT_DELAY_SECONDS)
                time.sleep(settings.RABBITMQ_RECONNECT_DELAY_SECONDS)
            except Exception as e:
                if self._should_stop:
                    break
                logger.exception("Unexpected error in criteria context worker: {}", e)
                time.sleep(settings.RABBITMQ_RECONNECT_DELAY_SECONDS)

    def _on_message(self, channel, method, properties, body):
        try:
            request = json.loads(body)
            job_id = request.get("jobId")
            logger.info("Received criteria context request for jobId={}", job_id)

            result = self._process_request(request)
            self._publish_result(result)

            channel.basic_ack(delivery_tag=method.delivery_tag)
            logger.info("Criteria context processed successfully for jobId={}", job_id)

        except Exception as e:
            logger.exception("Error processing criteria context request: {}", e)
            channel.basic_ack(delivery_tag=method.delivery_tag)

            # Publish failure result
            try:
                error_result = {
                    "jobId": request.get("jobId") if 'request' in dir() else None,
                    "status": "FAIL",
                    "errorMessage": str(e),
                    "contexts": None,
                    "criteriaTypeToScoringCriteriaId": request.get("criteriaTypeToScoringCriteriaId") if 'request' in dir() else None,
                }
                self._publish_result(error_result)
            except Exception:
                logger.exception("Failed to publish error result")

    def _process_request(self, request: dict) -> dict:
        """Process the criteria context request by calling GPT."""
        criteria_types = request.get("criteriaTypes", [])
        if not criteria_types:
            raise ValueError("No criteria types provided")

        # Build job data for prompt
        job_data = {
            "name": request.get("jobName"),
            "about": request.get("about"),
            "responsibilities": request.get("responsibilities"),
            "requirement": request.get("requirement"),
            "jobLevel": request.get("jobLevel"),
            "experienceTime": request.get("experienceTime"),
            "workingModel": request.get("workingModel"),
            "skills": request.get("skills", []),
            "domains": request.get("domains", []),
        }

        messages = build_criteria_context_prompt(job_data, criteria_types)
        model = getattr(settings, "OPENAI_CRITERIA_CONTEXT_MODEL", settings.OPENAI_MODEL)

        logger.info("Calling GPT model '{}' for criteria context (types={})", model, criteria_types)
        start = time.perf_counter()

        response = create_json_chat_completion(
            model=model,
            messages=messages,
            temperature=0.2,
            timeout=60,
        )

        content = response.choices[0].message.content
        elapsed_ms = (time.perf_counter() - start) * 1000

        try:
            contexts = json.loads(content)
        except json.JSONDecodeError as e:
            raise ValueError(f"GPT returned invalid JSON: {e}")

        # Filter to only requested types
        filtered = {k: v for k, v in contexts.items() if k in criteria_types}

        usage = response.usage
        logger.info(
            "Criteria context generated in {:.0f}ms, tokens={} (prompt={}, completion={})",
            elapsed_ms, usage.total_tokens, usage.prompt_tokens, usage.completion_tokens,
        )

        return {
            "jobId": request.get("jobId"),
            "status": "SUCCESS",
            "errorMessage": None,
            "contexts": filtered,
            "criteriaTypeToScoringCriteriaId": request.get("criteriaTypeToScoringCriteriaId"),
        }

    def _publish_result(self, result: dict):
        """Publish result to the result queue."""
        result_queue = settings.RABBITMQ_CRITERIA_CONTEXT_RESULT_QUEUE

        self._channel.queue_declare(queue=result_queue, durable=True)
        self._channel.basic_publish(
            exchange="",
            routing_key=result_queue,
            body=json.dumps(result),
            properties=pika.BasicProperties(
                delivery_mode=2,
                content_type="application/json",
            ),
        )
        logger.info("Published criteria context result for jobId={}, status={}",
                     result.get("jobId"), result.get("status"))
