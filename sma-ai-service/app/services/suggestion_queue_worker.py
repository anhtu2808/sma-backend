"""Background worker to handle suggestion requests using RabbitMQ and pika."""

import asyncio
import json
import threading
import time
from typing import Any

import pika
from loguru import logger
from pydantic import ValidationError

from app.core.config import settings
from app.schemas.suggestion import SuggestionRequestMessage, SuggestResultMessage
from app.services.suggestion_service import generate_suggestions


class SuggestionQueueWorker:
    """Background RabbitMQ consumer for suggestion requests."""

    def __init__(self) -> None:
        self._stop_event = threading.Event()
        self._thread: threading.Thread | None = None
        self._connection: pika.BlockingConnection | None = None
        self._channel: pika.adapters.blocking_connection.BlockingChannel | None = None

    def start(self) -> None:
        if not settings.RABBITMQ_ENABLED:
            logger.info("RabbitMQ is disabled. Suggestion queue worker won't start.")
            return

        if self._thread and self._thread.is_alive():
            logger.info("RabbitMQ suggestion worker is already running")
            return

        self._stop_event.clear()
        self._thread = threading.Thread(
            target=self._run,
            name="suggestion-rabbitmq-worker",
            daemon=True,
        )
        self._thread.start()
        logger.info("Started suggestion RabbitMQ worker")

    def stop(self) -> None:
        self._stop_event.set()

        if self._connection and self._connection.is_open:
            try:
                self._connection.add_callback_threadsafe(self._safe_stop_consuming)
            except Exception:
                logger.exception("Failed to request RabbitMQ suggestion consumer stop")

        if self._thread:
            self._thread.join(timeout=10)
        logger.info("Stopped suggestion RabbitMQ worker")

    def _safe_stop_consuming(self) -> None:
        if self._channel and self._channel.is_open:
            self._channel.stop_consuming()

    def _run(self) -> None:
        while not self._stop_event.is_set():
            try:
                self._connection = self._create_connection()
                self._channel = self._connection.channel()

                # Declare queues
                self._channel.queue_declare(
                    queue=settings.RABBITMQ_SUGGESTION_REQUEST_QUEUE, durable=True
                )
                self._channel.queue_declare(
                    queue=settings.RABBITMQ_SUGGESTION_RESULT_QUEUE, durable=True
                )
                self._channel.queue_declare(
                    queue=settings.RABBITMQ_RE_SUGGESTION_REQUEST_QUEUE, durable=True
                )
                self._channel.queue_declare(
                    queue=settings.RABBITMQ_RE_SUGGESTION_RESULT_QUEUE, durable=True
                )

                self._channel.basic_qos(prefetch_count=2)

                # Consume from normal queue
                self._channel.basic_consume(
                    queue=settings.RABBITMQ_SUGGESTION_REQUEST_QUEUE,
                    on_message_callback=lambda ch, method, props, body: self._on_message(
                        ch, method, props, body, queue_name=settings.RABBITMQ_SUGGESTION_REQUEST_QUEUE,
                        result_queue_name=settings.RABBITMQ_SUGGESTION_RESULT_QUEUE
                    ),
                    auto_ack=False,
                )

                # Consume from resuggest queue
                self._channel.basic_consume(
                    queue=settings.RABBITMQ_RE_SUGGESTION_REQUEST_QUEUE,
                    on_message_callback=lambda ch, method, props, body: self._on_message(
                        ch, method, props, body, queue_name=settings.RABBITMQ_RE_SUGGESTION_REQUEST_QUEUE,
                        result_queue_name=settings.RABBITMQ_RE_SUGGESTION_RESULT_QUEUE
                    ),
                    auto_ack=False,
                )

                logger.info(
                    "RabbitMQ suggestion worker is consuming queues '{}' and '{}'",
                    settings.RABBITMQ_SUGGESTION_REQUEST_QUEUE,
                    settings.RABBITMQ_RE_SUGGESTION_REQUEST_QUEUE,
                )
                self._channel.start_consuming()
            except Exception:
                logger.exception("RabbitMQ suggestion worker crashed, retrying...")
            finally:
                self._cleanup_connection()

            if not self._stop_event.is_set():
                time.sleep(settings.RABBITMQ_RECONNECT_DELAY_SECONDS)

    def _create_connection(self) -> pika.BlockingConnection:
        credentials = pika.PlainCredentials(
            username=settings.RABBITMQ_USER,
            password=settings.RABBITMQ_PASSWORD,
        )
        parameters = pika.ConnectionParameters(
            host=settings.RABBITMQ_HOST,
            port=settings.RABBITMQ_PORT,
            virtual_host=settings.RABBITMQ_VHOST,
            credentials=credentials,
            heartbeat=60,
            blocked_connection_timeout=300,
            connection_attempts=3,
            retry_delay=2,
        )
        return pika.BlockingConnection(parameters)

    def _cleanup_connection(self) -> None:
        if self._channel and self._channel.is_open:
            try:
                self._channel.close()
            except Exception:
                logger.exception("Failed to close RabbitMQ suggestion channel cleanly")

        if self._connection and self._connection.is_open:
            try:
                self._connection.close()
            except Exception:
                logger.exception("Failed to close RabbitMQ suggestion connection cleanly")

        self._channel = None
        self._connection = None

    def _on_message(
        self,
        ch: pika.adapters.blocking_connection.BlockingChannel,
        method,
        properties,
        body: bytes,
        queue_name: str,
        result_queue_name: str,
    ) -> None:
        evaluation_id = None
        try:
            raw_data = body.decode("utf-8")
            logger.info("Received suggestion request from {}:\n{}...", queue_name, raw_data[:200])
            
            payload = json.loads(raw_data)
            evaluation_id = payload.get("evaluationId")
            if evaluation_id is None:
                evaluation_id = -1

            # Validate incoming payload
            _ = SuggestionRequestMessage(**payload)
            logger.info("Processing suggestion request for evaluationId: {}", evaluation_id)

            # Execute suggestion logic (async service, so we use asyncio.run)
            suggestion_result = asyncio.run(generate_suggestions(payload))

            # Publish success result
            self._publish_result(
                routing_key=result_queue_name,
                parsed_data=suggestion_result.model_dump(mode="json"),
            )
            logger.info(
                "Successfully processed suggestion for evaluationId: {} from queue {}",
                evaluation_id, queue_name
            )

        except ValidationError as ve:
            error_msg = f"Failed to validate generated suggestion: {ve}"
            logger.exception(error_msg)
            self._send_error_result(ch, method, result_queue_name, evaluation_id, str(ve))
            return
            
        except TimeoutError as te:
            error_msg = f"Suggestion generation timed out: {te}"
            logger.exception(error_msg)
            self._send_error_result(ch, method, result_queue_name, evaluation_id, error_msg)
            return

        except Exception as suggestion_error:
            logger.exception("Failed to process suggestion message")

            if evaluation_id is None or evaluation_id == -1:
                try:
                    fallback_payload = json.loads(body.decode("utf-8"))
                    evaluation_id = fallback_payload.get("evaluationId")
                    if evaluation_id is None:
                        evaluation_id = -1
                except Exception:
                    evaluation_id = -1

            self._send_error_result(ch, method, result_queue_name, evaluation_id, str(suggestion_error)[:1000])
            return

        ch.basic_ack(delivery_tag=method.delivery_tag)

    def _send_error_result(self, ch, method, routing_key: str, evaluation_id: int, error_msg: str) -> None:
        try:
            error_result = SuggestResultMessage(
                evaluationId=evaluation_id,
                status="ERROR",
                errorMessage=error_msg,
                gapSuggestion=[],
                weaknessSuggestion=[]
            )
            self._publish_result(
                routing_key=routing_key,
                parsed_data=error_result.model_dump(mode="json"),
            )
            ch.basic_ack(delivery_tag=method.delivery_tag)
        except Exception:
            logger.exception("Failed to publish FAILED suggestion result, requeue message")
            ch.basic_nack(delivery_tag=method.delivery_tag, requeue=True)


    def _publish_result(
        self,
        *,
        routing_key: str,
        parsed_data: dict[str, Any],
    ) -> None:
        if self._channel is None or not self._channel.is_open:
            raise RuntimeError("RabbitMQ channel is not open for publishing")

        self._channel.basic_publish(
            exchange="",
            routing_key=routing_key,
            body=json.dumps(parsed_data, ensure_ascii=False).encode("utf-8"),
            properties=pika.BasicProperties(
                content_type="application/json",
                delivery_mode=2,
            ),
        )


suggestion_queue_worker = SuggestionQueueWorker()
