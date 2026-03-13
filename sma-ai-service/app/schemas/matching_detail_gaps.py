"""Pydantic schemas for detail matching analysis — adds aiExplanation, detail breakdowns, and suggestions."""

from typing import List, Literal, Optional

from pydantic import BaseModel, Field


MatchLevel = Literal["EXCELLENT", "GOOD", "FAIR", "POOR", "NOT_MATCHED"]
RelevanceType = Literal["HIGH", "MEDIUM", "LOW"]
LabelStatus = Literal["MISSING", "MATCHED"]
SkillLevel = Literal["NONE", "FRESHER", "JUNIOR", "MID", "SENIOR", "EXPERT"]


class CriteriaScoreDetailResult(BaseModel):
    """Detail item for a criteria score — represents a single evaluated element (skill, experience, education, etc.)."""

    label: str
    status: LabelStatus
    description: Optional[str] = None
    requiredLevel: Optional[SkillLevel] = None
    candidateLevel: Optional[SkillLevel] = None
    context: Optional[str] = None
    isRequired: Optional[bool] = None
    impactScore: Optional[float] = Field(default=None, ge=0, le=100)
    suggestions: List[str] = Field(default_factory=list)


class CriteriaScoreResult(BaseModel):
    """Evaluation result for a single scoring criterion with detail breakdowns."""

    criteriaId: int
    criteriaName: str
    aiScore: float = Field(ge=0, le=100)
    aiExplanation: Optional[str] = None
    details: List[CriteriaScoreDetailResult] = Field(default_factory=list)


class MatchingDetailSupplementResult(BaseModel):
    """Response model for detail supplement — detailed explanations and suggestions per criteria."""

    matchLevel: MatchLevel
    summary: Optional[str] = None
    strengths: Optional[str] = None
    weakness: Optional[str] = None
    isTrueLevel: Optional[bool] = None
    hasRelatedExperience: Optional[bool] = None
    transferabilityToRole: Optional[RelevanceType] = None
    processingTimeSecond: Optional[float] = None
    aiModelVersion: Optional[str] = None
    criteriaScores: List[CriteriaScoreResult] = Field(default_factory=list)
