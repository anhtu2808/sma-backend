"""
Background worker to handle suggestion requests.
Consumes messages from `suggest.request` and `re.suggest.request` queues, processes them with GPT,
and publishes results to `suggest.result` or `re.suggest.result`.
"""

import json
import asyncio
import traceback
from typing import Optional, Dict, Any

from fastapi.encoders import jsonable_encoder
from loguru import logger
import aio_pika
from pydantic import ValidationError

from app.core.config import settings
from app.schemas.suggestion import SuggestionRequestMessage, SuggestResultMessage
from app.services.suggestion_service import generate_suggestions


class SuggestionQueueWorker:
    def __init__(self):
        self.connection: Optional[aio_pika.RobustConnection] = None
        self.channel: Optional[aio_pika.Channel] = None
        self.should_run = False
        self._consume_task = None

    async def connect(self):
        """Establish connection to RabbitMQ"""
        if not settings.RABBITMQ_ENABLED:
            logger.info("RabbitMQ is disabled. Suggestion queue worker won't start.")
            return

        auth_str = ""
        if settings.RABBITMQ_USER and settings.RABBITMQ_PASSWORD:
            auth_str = f"{settings.RABBITMQ_USER}:{settings.RABBITMQ_PASSWORD}@"

        vhost = settings.RABBITMQ_VHOST
        if vhost == "/":
            vhost = "%2F"

        url = f"amqp://{auth_str}{settings.RABBITMQ_HOST}:{settings.RABBITMQ_PORT}/{vhost}"
        
        try:
            logger.info(f"Connecting to RabbitMQ at {settings.RABBITMQ_HOST}:{settings.RABBITMQ_PORT}")
            self.connection = await aio_pika.connect_robust(url)
            self.channel = await self.connection.channel()
            logger.info("Successfully connected to RabbitMQ for suggestion worker")
            return True
        except Exception as e:
            logger.error(f"Failed to connect to RabbitMQ for suggestion worker: {e}")
            return False

    async def initialize_queues(self):
        """Ensure queues exist"""
        if not self.channel:
            return

        # Declare normal queues
        await self.channel.declare_queue(
            settings.RABBITMQ_SUGGESTION_REQUEST_QUEUE, durable=True
        )
        await self.channel.declare_queue(
            settings.RABBITMQ_SUGGESTION_RESULT_QUEUE, durable=True
        )
        
        # Declare re-suggest queues
        await self.channel.declare_queue(
            settings.RABBITMQ_RE_SUGGESTION_REQUEST_QUEUE, durable=True
        )
        await self.channel.declare_queue(
            settings.RABBITMQ_RE_SUGGESTION_RESULT_QUEUE, durable=True
        )
        logger.info("Suggestion RabbitMQ queues initialized")

    async def _process_message(self, message: aio_pika.IncomingMessage, queue_name: str, result_queue_name: str):
        """Process a single suggestion request message"""
        try:
            async with message.process():
                # Parse message
                raw_data = message.body.decode()
                logger.info(f"Received suggestion request from {queue_name}:\n{raw_data[:200]}...")

                try:
                    data_dict = json.loads(raw_data)
                    # Validate incoming payload
                    request_config = SuggestionRequestMessage(**data_dict)
                except json.JSONDecodeError as e:
                    logger.error(f"Invalid JSON in suggestion request: {e}")
                    await self._send_error_result(result_queue_name, -1, f"Invalid JSON format: {str(e)}")
                    return
                except ValidationError as e:
                    logger.error(f"Invalid suggestion request schema: {e}")
                    # Attempt to extract evaluationId for the error message
                    eval_id = data_dict.get("evaluationId", -1) if isinstance(data_dict, dict) else -1
                    await self._send_error_result(result_queue_name, eval_id, f"Invalid schema: {str(e)}")
                    return

                eval_id = request_config.evaluationId
                logger.info(f"Processing suggestion request for evaluation_id: {eval_id}")

                try:
                    # Execute suggestion logic
                    result = await generate_suggestions(data_dict)

                    # Return result
                    await self._send_result(result_queue_name, result)
                    logger.info(f"Successfully processed suggestion for evaluation_id: {eval_id}")

                except ValidationError as ve:
                    error_msg = f"Failed to validate generated suggestion: {ve}"
                    logger.error(error_msg, exc_info=ve)
                    await self._send_error_result(result_queue_name, eval_id, str(ve))
                except TimeoutError as te:
                    error_msg = f"Suggestion generation timed out: {te}"
                    logger.error(error_msg, exc_info=te)
                    await self._send_error_result(result_queue_name, eval_id, error_msg)
                except Exception as ex:
                    logger.opt(exception=True).error(f"Error during suggestion processing: {ex}")
                    error_msg = f"Internal processing error: {str(ex)}"
                    await self._send_error_result(result_queue_name, eval_id, error_msg)

        except Exception as e:
            logger.opt(exception=True).error(f"Failed to process message wrapper: {e}")


    async def consume(self):
        """Consume messages from the suggestion queues concurrently"""
        while self.should_run:
            try:
                if not self.connection or self.connection.is_closed:
                    success = await self.connect()
                    if success:
                        await self.initialize_queues()
                    else:
                        await asyncio.sleep(settings.RABBITMQ_RECONNECT_DELAY_SECONDS)
                        continue

                # Set prefetch count for backpressure
                await self.channel.set_qos(prefetch_count=5)

                queue_normal = await self.channel.get_queue(settings.RABBITMQ_SUGGESTION_REQUEST_QUEUE)
                queue_resuggest = await self.channel.get_queue(settings.RABBITMQ_RE_SUGGESTION_REQUEST_QUEUE)
                
                logger.info("Started consuming suggestion requests")

                # Define concurrent processors
                async def process_normal(message: aio_pika.IncomingMessage):
                    await self._process_message(
                        message,
                        settings.RABBITMQ_SUGGESTION_REQUEST_QUEUE,
                        settings.RABBITMQ_SUGGESTION_RESULT_QUEUE
                    )

                async def process_resuggest(message: aio_pika.IncomingMessage):
                    await self._process_message(
                        message,
                        settings.RABBITMQ_RE_SUGGESTION_REQUEST_QUEUE,
                        settings.RABBITMQ_RE_SUGGESTION_RESULT_QUEUE
                    )

                normal_consumer_tag = await queue_normal.consume(process_normal)
                resuggest_consumer_tag = await queue_resuggest.consume(process_resuggest)

                try:
                    while self.should_run and not self.connection.is_closed:
                        await asyncio.sleep(1)
                finally:
                    # Clean up consumers if we exit the loop but connection isn't closed
                    if not self.connection.is_closed:
                        await queue_normal.cancel(normal_consumer_tag)
                        await queue_resuggest.cancel(resuggest_consumer_tag)

            except Exception as e:
                logger.error(f"Error in suggestion queue consumer: {e}")
                if self.should_run:
                    logger.info(f"Reconnecting in {settings.RABBITMQ_RECONNECT_DELAY_SECONDS} seconds...")
                    await asyncio.sleep(settings.RABBITMQ_RECONNECT_DELAY_SECONDS)

    async def _send_result(self, routing_key: str, result_dict: Dict[str, Any]):
        """Publish successful result to the result queue"""
        if not self.channel:
            logger.error(f"Cannot send result: no channel. RoutingKey: {routing_key}")
            return

        try:
            # Result dictionary should already be compliant with SuggestResultMessage schema
            json_str = json.dumps(jsonable_encoder(result_dict))

            message = aio_pika.Message(
                body=json_str.encode(),
                delivery_mode=aio_pika.DeliveryMode.PERSISTENT,
                content_type="application/json"
            )

            await self.channel.default_exchange.publish(
                message,
                routing_key=routing_key
            )
            eval_id = result_dict.get("evaluationId", "unknown")
            logger.info(f"Published suggestion result for evaluationId={eval_id} to {routing_key}")
        except Exception as e:
            logger.error(f"Failed to publish suggestion result: {e}")
            logger.debug(f"Result payload that failed to serialize/publish: {result_dict}")


    async def _send_error_result(self, routing_key: str, evaluation_id: int, error_msg: str):
        """Publish error result back over the specified routing queue"""
        if not self.channel:
            logger.error(f"Cannot send error result: no channel. Queue: {routing_key}")
            return

        try:
            error_result = SuggestResultMessage(
                evaluationId=evaluation_id,
                status="ERROR",
                errorMessage=error_msg,
                gapSuggestion=[],
                weaknessSuggestion=[]
            )

            json_str = json.dumps(jsonable_encoder(error_result.model_dump()))

            message = aio_pika.Message(
                body=json_str.encode(),
                delivery_mode=aio_pika.DeliveryMode.PERSISTENT,
                content_type="application/json"
            )

            await self.channel.default_exchange.publish(
                message,
                routing_key=routing_key
            )
            logger.info(f"Published suggestion error result for evaluationId={evaluation_id} to {routing_key}")
        except Exception as e:
            logger.error(f"Failed to publish suggestion error result: {e}")
            logger.debug(traceback.format_exc())

    def start(self):
        """Start the worker within the current event loop."""
        self.should_run = True
        self._consume_task = asyncio.create_task(self.consume())
        # Add a name for easier debugging if supported
        try:
            self._consume_task.set_name("suggestion_queue_worker")
        except AttributeError:
            pass
        logger.info("Suggestion queue worker started")

    def stop(self):
        """Stop the worker gracefully."""
        self.should_run = False
        if self._consume_task:
            self._consume_task.cancel()
        logger.info("Suggestion queue worker stopped")

    async def close_connection(self):
        """Close RabbitMQ connection manually."""
        if self.connection and not self.connection.is_closed:
            await self.connection.close()
            logger.info("Suggestion RabbitMQ connection closed")

# Export singleton instance
suggestion_queue_worker = SuggestionQueueWorker()
