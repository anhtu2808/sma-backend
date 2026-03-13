"""Pydantic schemas for overview matching analysis response validation."""

from typing import List, Literal, Optional

from pydantic import BaseModel, Field


MatchLevel = Literal["EXCELLENT", "GOOD", "FAIR", "POOR", "NOT_MATCHED"]
RelevanceType = Literal["HIGH", "MEDIUM", "LOW"]


class OverviewCriteriaScoreResult(BaseModel):
    """Lightweight criteria score — scores only, no explanations or nested details."""

    criteriaName: str
    aiScore: float = Field(ge=0, le=100)


class MatchingOverviewResult(BaseModel):
    """Root response model for overview matching analysis — concise scoring without deep details."""

    aiOverallScore: float = Field(ge=0, le=100)
    matchLevel: MatchLevel
    summary: Optional[str] = None
    strengths: Optional[str] = None
    weakness: Optional[str] = None
    isTrueLevel: Optional[bool] = None
    hasRelatedExperience: Optional[bool] = None
    transferabilityToRole: Optional[RelevanceType] = None
    aiModelVersion: Optional[str] = None
    processingTimeSecond: Optional[float] = None
    criteriaScores: List[OverviewCriteriaScoreResult] = Field(default_factory=list)
