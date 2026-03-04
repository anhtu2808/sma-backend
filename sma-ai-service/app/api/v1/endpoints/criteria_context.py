"""REST endpoint for generating scoring criteria context from job data."""

import json
import time

from fastapi import APIRouter, HTTPException
from loguru import logger
from pydantic import BaseModel, Field

from app.clients.openai_client import create_json_chat_completion
from app.core.config import settings
from app.prompts.criteria_context import build_criteria_context_prompt

router = APIRouter(prefix="/criteria", tags=["Criteria Context"])


class CriteriaContextRequest(BaseModel):
    """Request body for criteria context generation."""
    name: str | None = None
    about: str | None = None
    responsibilities: str | None = None
    requirement: str | None = None
    jobLevel: str | None = None
    experienceTime: int | None = None
    workingModel: str | None = None
    skills: list[str] = Field(default_factory=list)
    domains: list[str] = Field(default_factory=list)
    criteriaTypes: list[str]


class CriteriaContextResponse(BaseModel):
    """Response body with generated contexts."""
    contexts: dict[str, str]


@router.post("/generate-context", response_model=CriteriaContextResponse)
async def generate_criteria_context(request: CriteriaContextRequest):
    """
    Analyze job data and generate evaluation context for each requested criteria type.
    
    This is a synchronous call — the core backend waits for the response
    to populate ScoringCriteria.context before saving the job.
    """
    if not request.criteriaTypes:
        raise HTTPException(status_code=400, detail="criteriaTypes must not be empty")

    if not settings.OPENAI_API_KEY:
        raise HTTPException(status_code=500, detail="OPENAI_API_KEY is not configured")

    job_data = request.model_dump(exclude={"criteriaTypes"})
    messages = build_criteria_context_prompt(job_data, request.criteriaTypes)

    model = getattr(settings, "OPENAI_MATCHING_MODEL", settings.OPENAI_MODEL)

    logger.info(
        "Generating criteria context for types={}, job='{}'",
        request.criteriaTypes,
        request.name,
    )

    start = time.perf_counter()

    try:
        response = create_json_chat_completion(
            model=model,
            messages=messages,
            temperature=0.2,
            timeout=60,
        )

        content = response.choices[0].message.content
        elapsed_ms = (time.perf_counter() - start) * 1000

        try:
            contexts = json.loads(content)
        except json.JSONDecodeError as e:
            logger.error("GPT returned invalid JSON for criteria context: {}", e)
            raise HTTPException(status_code=502, detail=f"AI returned invalid JSON: {e}")

        # Filter to only requested types
        filtered = {k: v for k, v in contexts.items() if k in request.criteriaTypes}

        usage = response.usage
        logger.info(
            "Criteria context generated in {:.0f}ms, tokens={} (prompt={}, completion={})",
            elapsed_ms,
            usage.total_tokens,
            usage.prompt_tokens,
            usage.completion_tokens,
        )

        return CriteriaContextResponse(contexts=filtered)

    except HTTPException:
        raise
    except Exception as e:
        logger.exception("Failed to generate criteria context")
        raise HTTPException(status_code=500, detail=f"AI context generation failed: {str(e)}")
