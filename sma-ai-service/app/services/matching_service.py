"""
Matching analysis service - orchestrates GPT-based CV-JD matching evaluation.
"""

import time

from loguru import logger
from pydantic import ValidationError

from app.schemas.matching import MatchingResult
from app.services.matching_gpt_client import analyze_matching_with_gpt
from app.core.config import settings


async def analyze_matching(request_data: dict) -> MatchingResult:
    """
    Analyze how well a resume matches a job description.

    This is the main orchestration function that:
    1. Prepares context from resume data and job criteria
    2. Calls GPT for semantic matching analysis
    3. Validates the response against Pydantic schema

    Args:
        request_data: Matching request payload from core service.

    Returns:
        MatchingResult with structured evaluation data.

    Raises:
        ValueError: If analysis fails or validation errors occur.
        TimeoutError: If GPT request times out.
    """
    evaluation_id = request_data.get("evaluationId", "unknown")
    logger.info(
        "Starting matching analysis for evaluationId={}, jobId={}, resumeId={}",
        evaluation_id,
        request_data.get("jobId"),
        request_data.get("resumeId"),
    )
    start_total = time.perf_counter()

    # Step 1: Validate input has minimum required data
    if not request_data.get("criteria"):
        logger.warning(
            "No scoring criteria provided for evaluationId={}, using default analysis",
            evaluation_id,
        )

    # Step 2: Call GPT for matching analysis
    logger.info("Calling GPT for matching analysis")
    start_gpt = time.perf_counter()
    timeout = getattr(settings, "OPENAI_REQUEST_TIMEOUT", 120)
    parsed_data = analyze_matching_with_gpt(request_data, timeout=timeout)
    gpt_ms = (time.perf_counter() - start_gpt) * 1000
    logger.info(f"GPT matching analysis completed in {gpt_ms:.2f}ms")

    # Step 3: Validate against Pydantic schema
    logger.info("Validating matching result against schema")
    start_validate = time.perf_counter()
    try:
        matching_result = MatchingResult(**parsed_data)
    except ValidationError as e:
        logger.error(f"Matching result schema validation failed: {e}")
        raise ValueError(f"Matching result does not match expected schema: {str(e)}")
    validate_ms = (time.perf_counter() - start_validate) * 1000
    total_ms = (time.perf_counter() - start_total) * 1000

    logger.info(f"Schema validation completed in {validate_ms:.2f}ms")
    logger.info(f"Total matching analysis time: {total_ms:.2f}ms")
    logger.info(
        "Matching analysis completed: evaluationId={}, overallScore={}, matchLevel={}",
        evaluation_id,
        matching_result.aiOverallScore,
        matching_result.matchLevel,
    )

    return matching_result
