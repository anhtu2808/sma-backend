"""
Overview matching analysis service - orchestrates GPT-based quick CV-JD scoring.
"""

import time

from loguru import logger
from pydantic import ValidationError

from app.schemas.matching_overview import MatchingOverviewResult
from app.services.matching_gpt_client import analyze_matching_with_gpt
from app.prompts.matching_overview_analysis import build_matching_overview_prompt
from app.core.config import settings


async def analyze_matching_overview(request_data: dict) -> MatchingOverviewResult:
    """
    Quick overview scoring of how well a resume matches a job description.

    This function provides fast scoring without deep analysis:
    1. Prepares concise context from raw resume text and job criteria (with rules)
    2. Calls GPT with overview prompt (fewer tokens)
    3. Validates the response against lightweight Pydantic schema

    Args:
        request_data: Matching request payload from core service.

    Returns:
        MatchingOverviewResult with scores and brief summary.

    Raises:
        ValueError: If analysis fails or validation errors occur.
        TimeoutError: If GPT request times out.
    """
    evaluation_id = request_data.get("evaluationId", "unknown")
    logger.info(
        "Starting overview matching analysis for evaluationId={}, jobId={}, resumeId={}",
        evaluation_id,
        request_data.get("jobId"),
        request_data.get("resumeId"),
    )
    start_total = time.perf_counter()

    # Build overview prompt (concise format)
    messages = build_matching_overview_prompt(request_data)

    # Call GPT with overview prompt
    logger.info("Calling GPT for overview matching analysis")
    start_gpt = time.perf_counter()
    timeout = getattr(settings, "OPENAI_REQUEST_TIMEOUT", 60)

    from app.clients.openai_client import create_json_chat_completion
    import json

    model = getattr(settings, "OPENAI_MATCHING_OVERVIEW_MODEL", settings.OPENAI_MODEL)
    logger.info(f"Calling GPT model '{model}' for overview matching analysis")

    try:
        response = create_json_chat_completion(
            model=model,
            messages=messages,
            temperature=0.2,
            timeout=timeout,
        )

        content = response.choices[0].message.content
        gpt_ms = (time.perf_counter() - start_gpt) * 1000
        logger.info(f"GPT overview matching response received in {gpt_ms:.2f}ms")

        try:
            parsed_data = json.loads(content)
        except json.JSONDecodeError as e:
            logger.error(f"GPT returned invalid JSON for overview matching: {e}")
            raise ValueError(f"GPT returned invalid JSON: {str(e)}")

        # Add model info
        parsed_data["aiModelVersion"] = model
        parsed_data["processingTimeSecond"] = round(
            (time.perf_counter() - start_gpt), 2
        )

        # Log usage
        usage = response.usage
        logger.info(
            f"Overview matching token usage: {usage.total_tokens} "
            f"(Prompt: {usage.prompt_tokens}, Completion: {usage.completion_tokens})"
        )

    except Exception as e:
        if "Timeout" in str(type(e).__name__):
            raise TimeoutError("Overview matching analysis request timed out")
        raise

    # Validate against overview schema
    logger.info("Validating overview matching result against schema")
    try:
        overview_result = MatchingOverviewResult(**parsed_data)
    except ValidationError as e:
        logger.error(f"Overview matching result schema validation failed: {e}")
        raise ValueError(f"Overview result does not match expected schema: {str(e)}")

    total_ms = (time.perf_counter() - start_total) * 1000
    logger.info(f"Total overview matching analysis time: {total_ms:.2f}ms")
    logger.info(
        "Overview matching analysis completed: evaluationId={}, overallScore={}, matchLevel={}",
        evaluation_id,
        overview_result.aiOverallScore,
        overview_result.matchLevel,
    )

    return overview_result
