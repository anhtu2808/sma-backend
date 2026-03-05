"""
Suggestion service — interacts with GPT to generate career advice for identified gaps and weaknesses.
"""

import json
import time

from loguru import logger
from pydantic import ValidationError

from app.schemas.suggestion import SuggestionRequestMessage, SuggestResultMessage
from app.prompts.suggestion_analysis import build_suggestion_prompt
from app.core.config import settings

from typing import Dict, Any

async def generate_suggestions(request_data: Dict[str, Any]) -> Dict[str, Any]:
    """
    Generate actionable suggestions for gaps and weaknesses found in a resume evaluation.

    Args:
        request_data: The incoming SuggestionRequestMessage payload as a dict.

    Returns:
        A dict matching the SuggestResultMessage structure.

    Raises:
        ValueError: If analysis fails or validation errors occur.
        TimeoutError: If GPT request times out.
    """
    evaluation_id = request_data.get("evaluationId", -1)
    logger.info(
        "Starting suggestion generation for evaluationId={}, jobId={}, resumeId={}",
        evaluation_id,
        request_data.get("jobId"),
        request_data.get("resumeId"),
    )
    start_total = time.perf_counter()

    # Build prompt
    messages = build_suggestion_prompt(request_data)

    logger.info("Calling GPT for suggestion generation")
    start_gpt = time.perf_counter()
    timeout = getattr(settings, "OPENAI_REQUEST_TIMEOUT", 120)

    from app.clients.openai_client import create_json_chat_completion

    model = getattr(settings, "OPENAI_MATCHING_MODEL", settings.OPENAI_MODEL)
    logger.info(f"Calling GPT model '{model}' for suggestion generation")

    try:
        response = create_json_chat_completion(
            model=model,
            messages=messages,
            temperature=0.3,  # Slight creativity for advice formulation, but mostly grounded
            timeout=timeout,
        )

        content = response.choices[0].message.content
        gpt_ms = (time.perf_counter() - start_gpt) * 1000
        logger.info(f"GPT suggestion response received in {gpt_ms:.2f}ms")

        try:
            parsed_data = json.loads(content)
        except json.JSONDecodeError as e:
            logger.error(f"GPT returned invalid JSON for suggestions: {e}")
            raise ValueError(f"GPT returned invalid JSON: {str(e)}")

        # Log usage
        usage = response.usage
        logger.info(
            f"Suggestion token usage: {usage.total_tokens} "
            f"(Prompt: {usage.prompt_tokens}, Completion: {usage.completion_tokens})"
        )

    except Exception as e:
        if "Timeout" in str(type(e).__name__):
            raise TimeoutError("Suggestion generation request timed out")
        raise

    # Format result structure
    result = {
        "evaluationId": evaluation_id,
        "status": "SUCCESS",
        "errorMessage": None,
        "gapSuggestion": parsed_data.get("gapSuggestion", []),
        "weaknessSuggestion": parsed_data.get("weaknessSuggestion", [])
    }

    # Validate against schema
    logger.info("Validating suggestion result against schema")
    try:
        SuggestResultMessage(**result)
    except ValidationError as e:
        logger.error(f"Suggestion result schema validation failed: {e}")
        raise ValueError(f"Suggestion result does not match expected schema: {str(e)}")

    total_ms = (time.perf_counter() - start_total) * 1000
    logger.info(f"Total suggestion generation time: {total_ms:.2f}ms")
    logger.info(
        "Suggestion generation completed: evaluationId={}, gapSuggestions={}, weaknessSuggestions={}",
        evaluation_id,
        len(result["gapSuggestion"]),
        len(result["weaknessSuggestion"]),
    )

    return result
