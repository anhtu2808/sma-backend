"""Pydantic schemas for overview matching analysis response validation."""

from typing import List, Literal, Optional

from pydantic import BaseModel, Field


MatchLevel = Literal["EXCELLENT", "GOOD", "FAIR", "POOR", "NOT_MATCHED"]
CriteriaType = Literal["HARD_SKILLS", "SOFT_SKILLS", "EXPERIENCE", "EDUCATION", "JOB_TITLE", "JOB_LEVEL"]


class OverviewCriteriaScoreResult(BaseModel):
    """Lightweight criteria score — scores only, no explanations or nested details."""

    criteriaType: CriteriaType
    aiScore: float = Field(ge=0, le=100)
    maxScore: float = Field(default=100.0)
    weightedScore: Optional[float] = None


class MatchingOverviewResult(BaseModel):
    """Root response model for overview matching analysis — concise scoring without deep details."""

    aiOverallScore: float = Field(ge=0, le=100)
    matchLevel: MatchLevel
    summary: Optional[str] = None
    strengths: Optional[str] = None
    weakness: Optional[str] = None
    isSpecificJd: Optional[bool] = None
    aiModelVersion: Optional[str] = None
    criteriaScores: List[OverviewCriteriaScoreResult] = Field(default_factory=list)
