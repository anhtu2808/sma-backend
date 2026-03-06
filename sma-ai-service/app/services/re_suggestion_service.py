"""
Re-Suggestion service — interacts with GPT to generate career advice for a single identified weakness.
"""

import json
import time

from loguru import logger
from pydantic import ValidationError

from app.schemas.suggestion import ReSuggestRequestMessage, SuggestResultMessage
from app.prompts.re_suggestion_analysis import build_re_suggestion_prompt
from app.core.config import settings

from typing import Dict, Any

async def generate_re_suggestions(request_data: dict) -> SuggestResultMessage:
    """
    Generate actionable suggestions for a single weakness found in a resume evaluation.

    Args:
        request_data: The incoming ReSuggestRequestMessage payload as a dict.

    Returns:
        SuggestResultMessage with weakness suggestions.

    Raises:
        ValueError: If analysis fails or validation errors occur.
        TimeoutError: If GPT request times out.
    """
    evaluation_id = request_data.get("evaluationId")
    if evaluation_id is None:
        evaluation_id = -1
    logger.info(
        "Starting re-suggestion generation for evaluationId={}, jobId={}, resumeId={}",
        evaluation_id,
        request_data.get("jobId"),
        request_data.get("resumeId"),
    )
    start_total = time.perf_counter()

    # Build prompt
    messages = build_re_suggestion_prompt(request_data)

    logger.info("Calling GPT for re-suggestion generation")
    start_gpt = time.perf_counter()
    timeout = getattr(settings, "OPENAI_REQUEST_TIMEOUT", 120)

    from app.clients.openai_client import create_json_chat_completion

    model = getattr(settings, "OPENAI_MATCHING_MODEL", settings.OPENAI_MODEL)
    logger.info(f"Calling GPT model '{model}' for re-suggestion generation")

    try:
        response = create_json_chat_completion(
            model=model,
            messages=messages,
            temperature=0.3,
            timeout=timeout,
        )

        content = response.choices[0].message.content
        gpt_ms = (time.perf_counter() - start_gpt) * 1000
        logger.info(f"GPT re-suggestion response received in {gpt_ms:.2f}ms")

        try:
            parsed_data = json.loads(content)
        except json.JSONDecodeError as e:
            logger.error(f"GPT returned invalid JSON for re-suggestions: {e}")
            raise ValueError(f"GPT returned invalid JSON: {str(e)}")

        # Add identifying fields matching the SuggestResultMessage structure
        parsed_data["evaluationId"] = evaluation_id
        parsed_data["status"] = "SUCCESS"
        parsed_data["errorMessage"] = None
        
        # Log usage
        usage = response.usage
        logger.info(
            f"Re-Suggestion token usage: {usage.total_tokens} "
            f"(Prompt: {usage.prompt_tokens}, Completion: {usage.completion_tokens})"
        )

    except Exception as e:
        if "Timeout" in str(type(e).__name__):
            raise TimeoutError("Re-Suggestion generation request timed out")
        raise

    # Validate against schema
    logger.info("Validating re-suggestion result against schema")
    try:
        suggestion_result = SuggestResultMessage(**parsed_data)
    except ValidationError as e:
        logger.error(f"Re-Suggestion result schema validation failed: {e}")
        raise ValueError(f"Re-Suggestion result does not match expected schema: {str(e)}")

    total_ms = (time.perf_counter() - start_total) * 1000
    logger.info(f"Total re-suggestion generation time: {total_ms:.2f}ms")
    logger.info(
        "Re-Suggestion generation completed: evaluationId={}, weaknessSuggestions={}",
        evaluation_id,
        len(suggestion_result.weaknessSuggestion) if suggestion_result.weaknessSuggestion else 0,
    )

    return suggestion_result
