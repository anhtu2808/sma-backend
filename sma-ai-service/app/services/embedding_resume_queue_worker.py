"""Background worker to handle embedding resume requests using RabbitMQ and pika."""

import asyncio
import json
import threading
import time
from typing import Any

import pika
from loguru import logger
from pydantic import ValidationError

from app.core.config import settings
from app.schemas.embedding import EmbeddingResumeRequestMessage, EmbeddingResumeResultMessage
from app.services.embedding_resume_service import process_and_embed_resume


class EmbeddingResumeQueueWorker:
    """Background RabbitMQ consumer for resume embedding requests."""

    def __init__(self) -> None:
        self._stop_event = threading.Event()
        self._thread: threading.Thread | None = None
        self._connection: pika.BlockingConnection | None = None
        self._channel: pika.adapters.blocking_connection.BlockingChannel | None = None

    def start(self) -> None:
        if not settings.RABBITMQ_ENABLED:
            logger.info("RabbitMQ is disabled. Embedding queue worker won't start.")
            return

        if self._thread and self._thread.is_alive():
            logger.info("RabbitMQ embedding worker is already running")
            return

        self._stop_event.clear()
        self._thread = threading.Thread(
            target=self._run,
            name="embedding-rabbitmq-worker",
            daemon=True,
        )
        self._thread.start()
        logger.info("Started embedding RabbitMQ worker")

    def stop(self) -> None:
        self._stop_event.set()

        if self._connection and self._connection.is_open:
            try:
                self._connection.add_callback_threadsafe(self._safe_stop_consuming)
            except Exception:
                logger.exception("Failed to request RabbitMQ embedding consumer stop")

        if self._thread:
            self._thread.join(timeout=10)
        logger.info("Stopped embedding RabbitMQ worker")

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
                    queue=settings.RABBITMQ_EMBEDDING_RESUME_REQUEST_QUEUE, durable=True
                )
                self._channel.queue_declare(
                    queue=settings.RABBITMQ_EMBEDDING_RESUME_RESULT_QUEUE, durable=True
                )

                self._channel.basic_qos(prefetch_count=2)

                self._channel.basic_consume(
                    queue=settings.RABBITMQ_EMBEDDING_RESUME_REQUEST_QUEUE,
                    on_message_callback=lambda ch, method, props, body: self._on_message(
                        ch, method, props, body, queue_name=settings.RABBITMQ_EMBEDDING_RESUME_REQUEST_QUEUE,
                        result_queue_name=settings.RABBITMQ_EMBEDDING_RESUME_RESULT_QUEUE
                    ),
                    auto_ack=False,
                )

                logger.info(
                    "RabbitMQ embedding worker is consuming queue '{}'",
                    settings.RABBITMQ_EMBEDDING_RESUME_REQUEST_QUEUE
                )
                self._channel.start_consuming()
            except Exception:
                logger.exception("RabbitMQ embedding worker crashed, retrying...")
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
                logger.exception("Failed to close RabbitMQ embedding channel cleanly")

        if self._connection and self._connection.is_open:
            try:
                self._connection.close()
            except Exception:
                logger.exception("Failed to close RabbitMQ embedding connection cleanly")

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
        resume_id = None
        try:
            raw_data = body.decode("utf-8")
            logger.info("Received embedding request from {}:\n{}...", queue_name, raw_data[:200])
            
            payload = json.loads(raw_data)
            resume_id = payload.get("id")
            if resume_id is None:
                resume_id = -1

            # Execute validation and embedding flow (async service -> asyncio.run)
            result_msg = asyncio.run(process_and_embed_resume(payload))

            # Publish success result
            self._publish_result(
                routing_key=result_queue_name,
                parsed_data=result_msg.model_dump(mode="json"),
            )
            logger.info(
                "Successfully processed embedding for resumeId: {} from queue {}",
                resume_id, queue_name
            )

        except ValidationError as ve:
            error_msg = f"Failed to validate incoming embedding request: {ve}"
            logger.exception(error_msg)
            self._send_error_result(ch, method, result_queue_name, resume_id, str(ve))
            return
            
        except Exception as embed_err:
            logger.exception("Failed to process embedding message")
            if resume_id is None or resume_id == -1:
                try:
                    fallback_payload = json.loads(body.decode("utf-8"))
                    resume_id = fallback_payload.get("id", -1)
                except Exception:
                    resume_id = -1

            self._send_error_result(ch, method, result_queue_name, resume_id, str(embed_err)[:1000])
            return

        ch.basic_ack(delivery_tag=method.delivery_tag)

    def _send_error_result(self, ch, method, routing_key: str, resume_id: int, error_msg: str) -> None:
        try:
            error_result = EmbeddingResumeResultMessage(
                resumeId=resume_id,
                status="ERROR",
                errorMessage=error_msg,
            )
            self._publish_result(
                routing_key=routing_key,
                parsed_data=error_result.model_dump(mode="json"),
            )
            ch.basic_ack(delivery_tag=method.delivery_tag)
        except Exception:
            logger.exception("Failed to publish FAILED embedding result, requeue message")
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

embedding_resume_queue_worker = EmbeddingResumeQueueWorker()
