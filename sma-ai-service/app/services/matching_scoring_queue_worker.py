"""RabbitMQ worker to process CV-JD matching scoring requests asynchronously."""

import asyncio
import json
import threading
import time
from datetime import datetime, timezone
from enum import Enum
from typing import Any

import pika
from loguru import logger

from app.core.config import settings
from app.services.matching_service import analyze_matching
from app.services.matching_overview_service import analyze_matching_overview
from app.services.matching_detail_service import analyze_matching_detail_supplement


class EvaluationStatus(str, Enum):
    WAITING = "WAITING"
    PARTIAL = "PARTIAL"
    FINISH = "FINISH"
    FAIL = "FAIL"


class MatchingScoringQueueWorker:
    """Background RabbitMQ consumer for CV-JD matching scoring tasks."""

    def __init__(self) -> None:
        self._stop_event = threading.Event()
        self._thread: threading.Thread | None = None
        self._connection: pika.BlockingConnection | None = None
        self._channel: pika.adapters.blocking_connection.BlockingChannel | None = None

    def start(self) -> None:
        if not settings.RABBITMQ_ENABLED:
            logger.info("RabbitMQ matching worker is disabled by configuration")
            return

        if self._thread and self._thread.is_alive():
            logger.info("RabbitMQ matching worker is already running")
            return

        self._stop_event.clear()
        self._thread = threading.Thread(
            target=self._run,
            name="matching-scoring-rabbitmq-worker",
            daemon=True,
        )
        self._thread.start()
        logger.info("Started matching scoring RabbitMQ worker")

    def stop(self) -> None:
        self._stop_event.set()

        if self._connection and self._connection.is_open:
            try:
                self._connection.add_callback_threadsafe(self._safe_stop_consuming)
            except Exception:
                logger.exception("Failed to request RabbitMQ matching consumer stop")

        if self._thread:
            self._thread.join(timeout=10)
        logger.info("Stopped matching scoring RabbitMQ worker")

    def _safe_stop_consuming(self) -> None:
        if self._channel and self._channel.is_open:
            self._channel.stop_consuming()

    def _run(self) -> None:
        while not self._stop_event.is_set():
            try:
                self._connection = self._create_connection()
                self._channel = self._connection.channel()

                self._channel.queue_declare(
                    queue=settings.RABBITMQ_MATCHING_REQUEST_QUEUE,
                    durable=True,
                )
                self._channel.queue_declare(
                    queue=settings.RABBITMQ_MATCHING_RESULT_QUEUE,
                    durable=True,
                )
                self._channel.basic_qos(prefetch_count=1)
                self._channel.basic_consume(
                    queue=settings.RABBITMQ_MATCHING_REQUEST_QUEUE,
                    on_message_callback=self._on_message,
                    auto_ack=False,
                )

                logger.info(
                    "RabbitMQ matching worker is consuming queue '{}'",
                    settings.RABBITMQ_MATCHING_REQUEST_QUEUE,
                )
                self._channel.start_consuming()
            except Exception:
                logger.exception("RabbitMQ matching worker crashed, retrying...")
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
                logger.exception("Failed to close RabbitMQ matching channel cleanly")

        if self._connection and self._connection.is_open:
            try:
                self._connection.close()
            except Exception:
                logger.exception("Failed to close RabbitMQ matching connection cleanly")

        self._channel = None
        self._connection = None

    def _on_message(
        self,
        ch: pika.adapters.blocking_connection.BlockingChannel,
        method,
        properties,
        body: bytes,
    ) -> None:
        evaluation_id = None
        usage_event_id = None
        try:
            payload = json.loads(body.decode("utf-8"))
            evaluation_id = payload.get("evaluationId")
            usage_event_id = payload.get("usageEventId")

            if evaluation_id is None:
                raise ValueError("Invalid message payload: evaluationId is required")

            matching_type = payload.get("matchingType", "DETAIL")
            logger.info(
                "Received matching scoring task for evaluationId={}, jobId={}, resumeId={}, matchingType={}",
                evaluation_id,
                payload.get("jobId"),
                payload.get("resumeId"),
                matching_type,
            )

            # Route to appropriate matching analysis based on type
            if matching_type == "OVERVIEW":
                matching_result = asyncio.run(analyze_matching_overview(payload))
            elif matching_type == "DETAIL" and payload.get("overviewScores"):
                # Supplement mode: overview exists, add explanations/details/suggestions
                matching_result = asyncio.run(analyze_matching_detail_supplement(payload))
            else:
                # Full detail mode: no overview exists, do full matching
                matching_result = asyncio.run(analyze_matching(payload))

            # Publish success result
            self._publish_result(
                status=EvaluationStatus.FINISH,
                evaluation_id=evaluation_id,
                usage_event_id=usage_event_id,
                parsed_data=matching_result.model_dump(mode="json"),
                error_message=None,
            )
            logger.info(
                "Completed matching scoring for evaluationId={}, matchingType={}, score={}",
                evaluation_id,
                matching_type,
                getattr(matching_result, "aiOverallScore", None),
            )
        except Exception as matching_error:
            logger.exception("Failed to process matching scoring message")

            if evaluation_id is None:
                try:
                    fallback_payload = json.loads(body.decode("utf-8"))
                    evaluation_id = fallback_payload.get("evaluationId")
                    usage_event_id = fallback_payload.get("usageEventId")
                except Exception:
                    pass

            try:
                self._publish_result(
                    status=EvaluationStatus.FAIL,
                    evaluation_id=evaluation_id,
                    usage_event_id=usage_event_id,
                    parsed_data=None,
                    error_message=str(matching_error)[:1000],
                )
            except Exception:
                logger.exception(
                    "Failed to publish FAILED matching result, requeue message"
                )
                ch.basic_nack(delivery_tag=method.delivery_tag, requeue=True)
                return

        ch.basic_ack(delivery_tag=method.delivery_tag)

    def _publish_result(
        self,
        *,
        status: EvaluationStatus,
        evaluation_id: int | None,
        usage_event_id: int | None,
        parsed_data: dict[str, Any] | None,
        error_message: str | None,
    ) -> None:
        if self._channel is None or not self._channel.is_open:
            raise RuntimeError("RabbitMQ channel is not open for publishing")

        message = {
            "evaluationId": evaluation_id,
            "usageEventId": usage_event_id,
            "status": status.value,
            "errorMessage": error_message,
            "processedAt": datetime.now(timezone.utc).isoformat(),
            "parsedData": parsed_data,
        }

        self._channel.basic_publish(
            exchange="",
            routing_key=settings.RABBITMQ_MATCHING_RESULT_QUEUE,
            body=json.dumps(message, ensure_ascii=False).encode("utf-8"),
            properties=pika.BasicProperties(
                content_type="application/json",
                delivery_mode=2,
            ),
        )


matching_scoring_queue_worker = MatchingScoringQueueWorker()
