"""Pydantic schemas for full matching analysis response validation."""

from typing import List, Literal, Optional

from pydantic import BaseModel, Field


MatchLevel = Literal["EXCELLENT", "GOOD", "FAIR", "POOR", "NOT_MATCHED"]
CriteriaType = Literal["HARD_SKILLS", "SOFT_SKILLS", "EXPERIENCE", "EDUCATION", "JOB_TITLE", "JOB_LEVEL"]
RelevanceType = Literal["HIGH", "MEDIUM", "LOW"]
LabelStatus = Literal["MISSING", "MATCHED"]
SkillLevel = Literal["NONE", "FRESHER", "JUNIOR", "MID", "SENIOR", "EXPERT"]


class CriteriaScoreDetailResult(BaseModel):
    """Detail item for a criteria score — represents a single evaluated element."""

    label: str
    status: LabelStatus
    description: Optional[str] = None
    requiredLevel: Optional[SkillLevel] = None
    candidateLevel: Optional[SkillLevel] = None
    isRequired: Optional[bool] = None
    context: Optional[str] = None
    impactScore: Optional[float] = Field(default=None, ge=0, le=100)
    suggestions: List[str] = Field(default_factory=list)


class CriteriaScoreResult(BaseModel):
    """Evaluation result for a single scoring criterion."""

    criteriaType: CriteriaType
    aiScore: float = Field(ge=0, le=100)
    aiExplanation: Optional[str] = None
    details: List[CriteriaScoreDetailResult] = Field(default_factory=list)


class MatchingResult(BaseModel):
    """Root response model for full matching analysis."""

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
    criteriaScores: List[CriteriaScoreResult] = Field(default_factory=list)
