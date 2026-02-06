"""RabbitMQ worker to process resume parsing requests asynchronously."""

import asyncio
import json
import threading
import time
from datetime import datetime, timezone
from typing import Any

import pika
import requests
from loguru import logger

from app.core.config import settings
from app.services.resume_service import parse_resume


class ResumeParsingQueueWorker:
    """Background RabbitMQ consumer for resume parsing tasks."""

    def __init__(self) -> None:
        self._stop_event = threading.Event()
        self._thread: threading.Thread | None = None
        self._connection: pika.BlockingConnection | None = None
        self._channel: pika.adapters.blocking_connection.BlockingChannel | None = None

    def start(self) -> None:
        if not settings.RABBITMQ_ENABLED:
            logger.info("RabbitMQ worker is disabled by configuration")
            return

        if self._thread and self._thread.is_alive():
            logger.info("RabbitMQ worker is already running")
            return

        self._stop_event.clear()
        self._thread = threading.Thread(
            target=self._run,
            name="resume-parsing-rabbitmq-worker",
            daemon=True,
        )
        self._thread.start()
        logger.info("Started resume parsing RabbitMQ worker")

    def stop(self) -> None:
        self._stop_event.set()

        if self._connection and self._connection.is_open:
            try:
                self._connection.add_callback_threadsafe(self._safe_stop_consuming)
            except Exception:
                logger.exception("Failed to request RabbitMQ consumer stop")

        if self._thread:
            self._thread.join(timeout=10)
        logger.info("Stopped resume parsing RabbitMQ worker")

    def _safe_stop_consuming(self) -> None:
        if self._channel and self._channel.is_open:
            self._channel.stop_consuming()

    def _run(self) -> None:
        while not self._stop_event.is_set():
            try:
                self._connection = self._create_connection()
                self._channel = self._connection.channel()

                self._channel.queue_declare(
                    queue=settings.RABBITMQ_RESUME_PARSING_REQUEST_QUEUE,
                    durable=True,
                )
                self._channel.queue_declare(
                    queue=settings.RABBITMQ_RESUME_PARSING_RESULT_QUEUE,
                    durable=True,
                )
                self._channel.basic_qos(prefetch_count=1)
                self._channel.basic_consume(
                    queue=settings.RABBITMQ_RESUME_PARSING_REQUEST_QUEUE,
                    on_message_callback=self._on_message,
                    auto_ack=False,
                )

                logger.info(
                    "RabbitMQ worker is consuming queue '{}'",
                    settings.RABBITMQ_RESUME_PARSING_REQUEST_QUEUE,
                )
                self._channel.start_consuming()
            except Exception:
                logger.exception("RabbitMQ worker crashed, retrying...")
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
                logger.exception("Failed to close RabbitMQ channel cleanly")

        if self._connection and self._connection.is_open:
            try:
                self._connection.close()
            except Exception:
                logger.exception("Failed to close RabbitMQ connection cleanly")

        self._channel = None
        self._connection = None

    def _on_message(
        self,
        ch: pika.adapters.blocking_connection.BlockingChannel,
        method,
        properties,
        body: bytes,
    ) -> None:
        try:
            payload = json.loads(body.decode("utf-8"))
            resume_id = payload.get("resumeId")
            resume_url = payload.get("resumeUrl")

            if resume_id is None or not resume_url:
                raise ValueError("Invalid message payload: resumeId and resumeUrl are required")

            logger.info("Received resume parsing task for resumeId={}", resume_id)
            file_bytes = self._download_resume_file(resume_url)
            parsed_resume = asyncio.run(parse_resume(file_bytes))

            self._publish_result(
                status="SUCCESS",
                resume_id=resume_id,
                parsed_data=parsed_resume.model_dump(mode="json"),
                error_message=None,
            )
            logger.info("Completed resume parsing task for resumeId={}", resume_id)
        except Exception as parsing_error:
            logger.exception("Failed to process resume parsing message")

            resume_id = None
            try:
                fallback_payload = json.loads(body.decode("utf-8"))
                resume_id = fallback_payload.get("resumeId")
            except Exception:
                pass

            try:
                self._publish_result(
                    status="FAILED",
                    resume_id=resume_id,
                    parsed_data=None,
                    error_message=str(parsing_error)[:1000],
                )
            except Exception:
                logger.exception("Failed to publish FAILED parsing result, requeue message")
                ch.basic_nack(delivery_tag=method.delivery_tag, requeue=True)
                return

        ch.basic_ack(delivery_tag=method.delivery_tag)

    def _download_resume_file(self, resume_url: str) -> bytes:
        response = requests.get(
            resume_url,
            timeout=(10, 120),
        )
        response.raise_for_status()
        return response.content

    def _publish_result(
        self,
        *,
        status: str,
        resume_id: int | None,
        parsed_data: dict[str, Any] | None,
        error_message: str | None,
    ) -> None:
        if self._channel is None or not self._channel.is_open:
            raise RuntimeError("RabbitMQ channel is not open for publishing")

        message = {
            "resumeId": resume_id,
            "status": status,
            "errorMessage": error_message,
            "processedAt": datetime.now(timezone.utc).isoformat(),
            "parsedData": parsed_data,
        }

        self._channel.basic_publish(
            exchange="",
            routing_key=settings.RABBITMQ_RESUME_PARSING_RESULT_QUEUE,
            body=json.dumps(message, ensure_ascii=False).encode("utf-8"),
            properties=pika.BasicProperties(
                content_type="application/json",
                delivery_mode=2,
            ),
        )


resume_parsing_queue_worker = ResumeParsingQueueWorker()
