"""OpenAI GPT client for resume parsing."""

import json
import time
from datetime import datetime, timezone
from openai import APITimeoutError, APIError
from loguru import logger

from app.clients.openai_client import create_json_chat_completion
from app.core.config import settings
from app.prompts.resume_parsing import build_resume_parsing_prompt


# Pricing per 1M tokens (approximate)
PRICING = {
    "gpt-4o": {"input": 2.50, "output": 10.00},
    "gpt-4o-mini": {"input": 0.15, "output": 0.60},
    "gpt-4-turbo-preview": {"input": 10.00, "output": 30.00},
    "gpt-4-turbo": {"input": 10.00, "output": 30.00},
    "gpt-4.1": {"input": 10.00, "output": 30.00},  # Assign same as turbo for now if unknown
    "gpt-3.5-turbo": {"input": 0.50, "output": 1.50},
}

def calculate_cost(model: str, prompt_tokens: int, completion_tokens: int) -> float:
    """
    Calculate estimated cost for the API call.
    
    Args:
        model: Model name used
        prompt_tokens: Number of input tokens
        completion_tokens: Number of output tokens
        
    Returns:
        Estimated cost in USD
    """
    # Default to gpt-4-turbo pricing if model not found
    price = PRICING.get(model, PRICING["gpt-4-turbo"])
    
    input_cost = (prompt_tokens / 1_000_000) * price["input"]
    output_cost = (completion_tokens / 1_000_000) * price["output"]
    
    return round(input_cost + output_cost, 6)


def parse_resume_with_gpt(resume_text: str, timeout: int = 60) -> dict:
    """
    Call GPT API to parse resume text into structured data.
    
    Args:
        resume_text: Cleaned resume text content
        timeout: Request timeout in seconds
        
    Returns:
        Parsed resume data as dictionary
        
    Raises:
        TimeoutError: If GPT API times out
        ValueError: If GPT returns invalid JSON
    """
    if not settings.OPENAI_API_KEY:
        raise ValueError("OPENAI_API_KEY is not configured")

    messages = build_resume_parsing_prompt(resume_text)
    model = getattr(settings, "OPENAI_RESUME_MODEL", settings.OPENAI_MODEL)

    logger.info(f"Calling GPT model '{model}' for resume parsing")
    logger.debug(f"Resume text length sent to GPT: {len(resume_text)} characters")
    start_call = time.perf_counter()

    try:
        response = create_json_chat_completion(
            model=model,
            messages=messages,
            temperature=0.1,  # Low temperature for consistent parsing
            timeout=timeout,
        )

        content = response.choices[0].message.content
        call_ms = (time.perf_counter() - start_call) * 1000
        logger.info(f"OpenAI response received in {call_ms:.2f}ms")
        logger.debug(f"GPT response length: {len(content)} characters")

        # Parse JSON response
        try:
            parsed_data = json.loads(content)
        except json.JSONDecodeError as e:
            logger.error(f"GPT returned invalid JSON: {e}")
            raise ValueError(f"GPT returned invalid JSON: {str(e)}")

        # Extract usage statistics
        usage = response.usage
        prompt_tokens = usage.prompt_tokens
        completion_tokens = usage.completion_tokens
        total_tokens = usage.total_tokens

        # Calculate cost
        cost = calculate_cost(model, prompt_tokens, completion_tokens)

        logger.info(f"Token usage: {total_tokens} (Prompt: {prompt_tokens}, Completion: {completion_tokens})")
        logger.info(f"Estimated cost: ${cost:.6f}")

        # Add metadata
        if "metadata" not in parsed_data:
            parsed_data["metadata"] = {}

        parsed_data["metadata"]["parsedBy"] = model
        parsed_data["metadata"]["parsedAt"] = datetime.now(timezone.utc).isoformat()
        parsed_data["metadata"]["sourceType"] = "pdf"

        # Add usage and cost to metadata
        parsed_data["metadata"]["usage"] = {
            "promptTokens": prompt_tokens,
            "completionTokens": completion_tokens,
            "totalTokens": total_tokens,
        }
        parsed_data["metadata"]["costUsd"] = cost

        logger.info("Resume parsing completed successfully")
        return parsed_data

    except APITimeoutError:
        logger.error("GPT API request timed out")
        raise TimeoutError("Resume parsing request timed out")

    except APIError as e:
        logger.error(f"GPT API error: {e}")
        raise ValueError(f"GPT API error: {str(e)}")
