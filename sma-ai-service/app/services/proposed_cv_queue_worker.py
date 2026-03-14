"""Background worker to handle proposed CV matching requests using RabbitMQ and pika."""

import json
import threading
import time
from typing import Any

import pika
from loguru import logger

from app.core.config import settings
from app.schemas.proposed_cv import ProposeStatus
from app.services.proposed_cv_service import find_proposed_resumes


class ProposedCVQueueWorker:
    """Background RabbitMQ consumer for proposed CV matching requests."""

    def __init__(self) -> None:
        self._stop_event = threading.Event()
        self._thread: threading.Thread | None = None
        self._connection: pika.BlockingConnection | None = None
        self._channel: pika.adapters.blocking_connection.BlockingChannel | None = None

    def start(self) -> None:
        if not settings.RABBITMQ_ENABLED:
            logger.info("RabbitMQ is disabled. Proposed CV worker won't start.")
            return

        if self._thread and self._thread.is_alive():
            logger.info("RabbitMQ proposed CV worker is already running")
            return

        self._stop_event.clear()
        self._thread = threading.Thread(
            target=self._run,
            name="proposed-cv-rabbitmq-worker",
            daemon=True,
        )
        self._thread.start()
        logger.info("Started proposed CV RabbitMQ worker")

    def stop(self) -> None:
        self._stop_event.set()

        if self._connection and self._connection.is_open:
            try:
                self._connection.add_callback_threadsafe(self._safe_stop_consuming)
            except Exception:
                logger.exception("Failed to request RabbitMQ proposed CV consumer stop")

        if self._thread:
            self._thread.join(timeout=10)
        logger.info("Stopped proposed CV RabbitMQ worker")

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
                    queue=settings.RABBITMQ_PROPOSED_CV_REQUEST_QUEUE, durable=True
                )
                self._channel.queue_declare(
                    queue=settings.RABBITMQ_PROPOSED_CV_RESULT_QUEUE, durable=True
                )

                self._channel.basic_qos(prefetch_count=1)

                self._channel.basic_consume(
                    queue=settings.RABBITMQ_PROPOSED_CV_REQUEST_QUEUE,
                    on_message_callback=self._on_message,
                    auto_ack=False,
                )

                logger.info(
                    "RabbitMQ proposed CV worker is consuming queue '{}'",
                    settings.RABBITMQ_PROPOSED_CV_REQUEST_QUEUE,
                )
                self._channel.start_consuming()
            except Exception:
                logger.exception("RabbitMQ proposed CV worker crashed, retrying...")
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
                logger.exception("Failed to close RabbitMQ proposed CV channel cleanly")

        if self._connection and self._connection.is_open:
            try:
                self._connection.close()
            except Exception:
                logger.exception("Failed to close RabbitMQ proposed CV connection cleanly")

        self._channel = None
        self._connection = None

    def _on_message(
        self,
        ch: pika.adapters.blocking_connection.BlockingChannel,
        method,
        properties,
        body: bytes,
    ) -> None:
        job_id = None
        try:
            raw_data = body.decode("utf-8")
            logger.info("Received proposed CV request: {}", raw_data[:200])

            payload = json.loads(raw_data)
            job_id = payload.get("id")

            # Execute the matching logic (synchronous — no asyncio needed)
            result_msg = find_proposed_resumes(payload)

            # Publish result
            self._publish_result(
                parsed_data=result_msg.model_dump(mode="json"),
            )
            logger.info(
                "Successfully processed proposed CV for jobId={}, found {} matches",
                job_id,
                len(result_msg.proposedCVs),
            )

        except Exception as e:
            logger.exception("Failed to process proposed CV message for jobId={}", job_id)
            # On error, publish an empty result so the core service is not left hanging
            try:
                empty_result = {
                    "status": ProposeStatus.FAILED.value,
                    "errorMessage": str(e),
                    "jobId": job_id or -1,
                    "proposedCVs": []
                }
                self._publish_result(parsed_data=empty_result)
            except Exception:
                logger.exception("Failed to publish empty proposed CV result, requeue message")
                ch.basic_nack(delivery_tag=method.delivery_tag, requeue=True)
                return

        ch.basic_ack(delivery_tag=method.delivery_tag)

    def _publish_result(
        self,
        *,
        parsed_data: dict[str, Any],
    ) -> None:
        if self._channel is None or not self._channel.is_open:
            raise RuntimeError("RabbitMQ channel is not open for publishing")

        self._channel.basic_publish(
            exchange="",
            routing_key=settings.RABBITMQ_PROPOSED_CV_RESULT_QUEUE,
            body=json.dumps(parsed_data, ensure_ascii=False).encode("utf-8"),
            properties=pika.BasicProperties(
                content_type="application/json",
                delivery_mode=2,
            ),
        )


proposed_cv_queue_worker = ProposedCVQueueWorker()
