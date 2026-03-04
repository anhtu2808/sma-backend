"""OpenAI GPT client for CV-JD matching analysis."""

import json
import time
from datetime import datetime, timezone

from loguru import logger
from openai import APIError, APITimeoutError

from app.clients.openai_client import create_json_chat_completion
from app.core.config import settings
from app.prompts.matching_analysis import build_matching_analysis_prompt


# Pricing per 1M tokens (approximate)
PRICING = {
    "gpt-4o": {"input": 2.50, "output": 10.00},
    "gpt-4o-mini": {"input": 0.15, "output": 0.60},
    "gpt-4-turbo-preview": {"input": 10.00, "output": 30.00},
    "gpt-4-turbo": {"input": 10.00, "output": 30.00},
    "gpt-3.5-turbo": {"input": 0.50, "output": 1.50},
}


def calculate_cost(model: str, prompt_tokens: int, completion_tokens: int) -> float:
    """Calculate estimated cost for the API call."""
    price = PRICING.get(model, PRICING["gpt-4o-mini"])
    input_cost = (prompt_tokens / 1_000_000) * price["input"]
    output_cost = (completion_tokens / 1_000_000) * price["output"]
    return round(input_cost + output_cost, 6)


def analyze_matching_with_gpt(request_data: dict, timeout: int = 120) -> dict:
    """
    Call GPT API to analyze CV-JD matching.

    Args:
        request_data: Matching request data with job criteria and resume info.
        timeout: Request timeout in seconds.

    Returns:
        Parsed matching analysis as dictionary.

    Raises:
        TimeoutError: If GPT API times out.
        ValueError: If GPT returns invalid JSON.
    """
    if not settings.OPENAI_API_KEY:
        raise ValueError("OPENAI_API_KEY is not configured")

    messages = build_matching_analysis_prompt(request_data)
    model = getattr(settings, "OPENAI_MATCHING_MODEL", settings.OPENAI_MODEL)

    logger.info(f"Calling GPT model '{model}' for matching analysis")
    start_call = time.perf_counter()

    try:
        response = create_json_chat_completion(
            model=model,
            messages=messages,
            temperature=0.2,
            timeout=timeout,
        )

        content = response.choices[0].message.content
        call_ms = (time.perf_counter() - start_call) * 1000
        logger.info(f"OpenAI matching response received in {call_ms:.2f}ms")

        # Parse JSON response
        try:
            parsed_data = json.loads(content)
        except json.JSONDecodeError as e:
            logger.error(f"GPT returned invalid JSON for matching: {e}")
            raise ValueError(f"GPT returned invalid JSON: {str(e)}")

        # Extract usage statistics
        usage = response.usage
        prompt_tokens = usage.prompt_tokens
        completion_tokens = usage.completion_tokens
        total_tokens = usage.total_tokens

        # Calculate cost
        cost = calculate_cost(model, prompt_tokens, completion_tokens)

        logger.info(
            f"Matching analysis token usage: {total_tokens} "
            f"(Prompt: {prompt_tokens}, Completion: {completion_tokens})"
        )
        logger.info(f"Matching analysis estimated cost: ${cost:.6f}")

        # Add model info to response
        parsed_data["aiModelVersion"] = model
        parsed_data["processingTimeSecond"] = round(
            (time.perf_counter() - start_call), 2
        )

        logger.info("Matching analysis completed successfully")
        return parsed_data

    except APITimeoutError:
        logger.error("GPT API request timed out for matching analysis")
        raise TimeoutError("Matching analysis request timed out")

    except APIError as e:
        logger.error(f"GPT API error during matching analysis: {e}")
        raise ValueError(f"GPT API error: {str(e)}")
