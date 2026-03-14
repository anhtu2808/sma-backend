"""
Re-suggestion service that generates one updated suggestion for a weakness.
"""

import json
import time
from typing import Any

from loguru import logger
from pydantic import ValidationError

from app.core.config import settings
from app.prompts.re_suggestion_analysis import build_re_suggestion_prompt
from app.schemas.suggestion import SuggestResultMessage


def _normalize_re_suggestion_result(
    parsed_data: dict[str, Any], request_data: dict[str, Any]
) -> dict[str, Any]:
    """Normalize GPT output to the current SuggestResultMessage schema."""
    suggestion_text = parsed_data.get("suggestion")

    if not suggestion_text:
        weakness_suggestions = parsed_data.get("weaknessSuggestion") or []
        if weakness_suggestions and isinstance(weakness_suggestions, list):
            first_item = weakness_suggestions[0] or {}
            suggestion_text = first_item.get("suggestion")

    return {
        "suggestionId": parsed_data.get("suggestionId", request_data.get("suggestionId")),
        "suggestion": suggestion_text,
        "status": parsed_data.get("status", "SUCCESS"),
        "errorMessage": parsed_data.get("errorMessage"),
    }


async def generate_re_suggestions(request_data: dict) -> SuggestResultMessage:
    """Generate one actionable suggestion for the provided weakness."""
    suggestion_id = request_data.get("suggestionId")
    logger.info(
        "Starting re-suggestion generation for suggestionId={}, jobTitle={}",
        suggestion_id,
        request_data.get("jobTitle"),
    )
    start_total = time.perf_counter()

    messages = build_re_suggestion_prompt(request_data)

    logger.info("Calling GPT for re-suggestion generation")
    start_gpt = time.perf_counter()
    timeout = getattr(settings, "OPENAI_REQUEST_TIMEOUT", 120)

    from app.clients.openai_client import create_json_chat_completion

    model = getattr(settings, "OPENAI_MATCHING_MODEL", settings.OPENAI_MODEL)
    logger.info("Calling GPT model '{}' for re-suggestion generation", model)

    try:
        response = create_json_chat_completion(
            model=model,
            messages=messages,
            temperature=0.3,
            timeout=timeout,
        )

        content = response.choices[0].message.content
        gpt_ms = (time.perf_counter() - start_gpt) * 1000
        logger.info("GPT re-suggestion response received in {:.2f}ms", gpt_ms)

        try:
            parsed_data = json.loads(content)
        except json.JSONDecodeError as e:
            logger.error("GPT returned invalid JSON for re-suggestions: {}", e)
            raise ValueError(f"GPT returned invalid JSON: {str(e)}") from e

        parsed_data = _normalize_re_suggestion_result(parsed_data, request_data)
        parsed_data["status"] = "SUCCESS"
        parsed_data["errorMessage"] = None

        usage = response.usage
        logger.info(
            "Re-suggestion token usage: {} (Prompt: {}, Completion: {})",
            usage.total_tokens,
            usage.prompt_tokens,
            usage.completion_tokens,
        )

    except Exception as e:
        if "Timeout" in str(type(e).__name__):
            raise TimeoutError("Re-suggestion generation request timed out") from e
        raise

    logger.info("Validating re-suggestion result against schema")
    try:
        suggestion_result = SuggestResultMessage(**parsed_data)
    except ValidationError as e:
        logger.error("Re-suggestion result schema validation failed: {}", e)
        raise ValueError(
            f"Re-suggestion result does not match expected schema: {str(e)}"
        ) from e

    total_ms = (time.perf_counter() - start_total) * 1000
    logger.info("Total re-suggestion generation time: {:.2f}ms", total_ms)
    logger.info(
        "Re-suggestion generation completed: suggestionId={}, hasSuggestion={}",
        suggestion_result.suggestionId,
        bool(suggestion_result.suggestion),
    )

    return suggestion_result
