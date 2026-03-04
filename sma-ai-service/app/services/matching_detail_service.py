"""
Detail supplement matching service — adds explanations, nested skills, gaps & weaknesses
to an existing overview evaluation.
"""

import json
import time

from loguru import logger
from pydantic import ValidationError

from app.schemas.matching_detail_gaps import MatchingDetailSupplementResult
from app.prompts.matching_detail_gaps_analysis import build_matching_detail_supplement_prompt
from app.core.config import settings


async def analyze_matching_detail_supplement(request_data: dict) -> MatchingDetailSupplementResult:
    """
    Supplement an existing overview evaluation with detailed analysis.

    This function provides:
    1. Per-criteria explanations (aiExplanation)
    2. Nested skill breakdowns (hardSkills, softSkills, experienceDetails)
    3. Gap analysis between JD requirements and CV capabilities
    4. Detailed weakness analysis
    5. isTrueLevel and hasRelatedExperience

    Args:
        request_data: Matching request payload including overview scores as context.

    Returns:
        MatchingDetailSupplementResult with supplementary analysis data.

    Raises:
        ValueError: If analysis fails or validation errors occur.
        TimeoutError: If GPT request times out.
    """
    evaluation_id = request_data.get("evaluationId", "unknown")
    logger.info(
        "Starting detail supplement analysis for evaluationId={}, jobId={}, resumeId={}",
        evaluation_id,
        request_data.get("jobId"),
        request_data.get("resumeId"),
    )
    start_total = time.perf_counter()

    # Build detail supplement prompt (includes overview scores as context)
    messages = build_matching_detail_supplement_prompt(request_data)

    # Call GPT
    logger.info("Calling GPT for detail supplement analysis")
    start_gpt = time.perf_counter()
    timeout = getattr(settings, "OPENAI_REQUEST_TIMEOUT", 120)

    from app.clients.openai_client import create_json_chat_completion

    model = getattr(settings, "OPENAI_MATCHING_MODEL", settings.OPENAI_MODEL)
    logger.info(f"Calling GPT model '{model}' for detail supplement analysis")

    try:
        response = create_json_chat_completion(
            model=model,
            messages=messages,
            temperature=0.2,
            timeout=timeout,
        )

        content = response.choices[0].message.content
        gpt_ms = (time.perf_counter() - start_gpt) * 1000
        logger.info(f"GPT detail supplement response received in {gpt_ms:.2f}ms")

        try:
            parsed_data = json.loads(content)
        except json.JSONDecodeError as e:
            logger.error(f"GPT returned invalid JSON for detail supplement: {e}")
            raise ValueError(f"GPT returned invalid JSON: {str(e)}")

        # Add processing time
        parsed_data["processingTimeSecond"] = round(time.perf_counter() - start_gpt, 2)

        # Log usage
        usage = response.usage
        logger.info(
            f"Detail supplement token usage: {usage.total_tokens} "
            f"(Prompt: {usage.prompt_tokens}, Completion: {usage.completion_tokens})"
        )

    except Exception as e:
        if "Timeout" in str(type(e).__name__):
            raise TimeoutError("Detail supplement analysis request timed out")
        raise

    # Validate against schema
    logger.info("Validating detail supplement result against schema")
    try:
        detail_result = MatchingDetailSupplementResult(**parsed_data)
    except ValidationError as e:
        logger.error(f"Detail supplement result schema validation failed: {e}")
        raise ValueError(f"Detail supplement result does not match expected schema: {str(e)}")

    total_ms = (time.perf_counter() - start_total) * 1000
    logger.info(f"Total detail supplement analysis time: {total_ms:.2f}ms")
    logger.info(
        "Detail supplement analysis completed: evaluationId={}, criteriaScores={}, gaps={}, weaknesses={}",
        evaluation_id,
        len(detail_result.criteriaScores),
        len(detail_result.gaps),
        len(detail_result.weaknesses),
    )

    return detail_result
