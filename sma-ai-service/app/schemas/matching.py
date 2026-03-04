"""Pydantic schemas for matching analysis response validation."""

from typing import List, Literal, Optional

from pydantic import BaseModel, Field


MatchLevel = Literal["EXCELLENT", "GOOD", "FAIR", "POOR", "NOT_MATCHED"]
SkillCategory = Literal["PROGRAMMING_LANGUAGE", "FRAMEWORK", "TOOL", "DATABASE", "OTHER"]
SkillLevel = Literal["JUNIOR", "MID", "SENIOR", "EXPERT"]
RelevanceType = Literal["HIGH", "MEDIUM", "LOW"]
GapType = Literal["HARD_SKILL", "SOFT_SKILL", "EXPERIENCE", "EDUCATION", "CERTIFICATION"]
ImpactType = Literal["CRITICAL", "HIGH", "MEDIUM", "LOW"]
CriteriaType = Literal["HARD_SKILLS", "SOFT_SKILLS", "EXPERIENCE", "EDUCATION", "JOB_TITLE", "JOB_LEVEL"]


class HardSkillResult(BaseModel):
    """Evaluation result for a single hard skill."""

    skillName: str
    evidence: Optional[str] = None
    skillCategory: Optional[SkillCategory] = None
    requiredLevel: Optional[SkillLevel] = None
    candidateLevel: Optional[SkillLevel] = None
    matchScore: Optional[float] = Field(default=None, ge=0, le=100)
    yearsOfExperience: Optional[float] = None
    isCritical: Optional[bool] = None
    isMatched: Optional[bool] = None
    isMissing: Optional[bool] = None
    isExtra: Optional[bool] = None
    relevance: Optional[RelevanceType] = None


class SoftSkillResult(BaseModel):
    """Evaluation result for a single soft skill."""

    skillName: str
    evidence: Optional[str] = None
    isRequired: Optional[bool] = None
    isFound: Optional[bool] = None


class ExperienceDetailResult(BaseModel):
    """Evaluation result for a single experience entry."""

    companyName: Optional[str] = None
    position: Optional[str] = None
    durationMonths: Optional[int] = None
    keyAchievements: Optional[str] = None
    technologiesUsed: Optional[str] = None
    isRelevant: Optional[bool] = None
    transferabilityToRole: Optional[RelevanceType] = None
    experienceGravity: Optional[RelevanceType] = None


class CriteriaScoreResult(BaseModel):
    """Evaluation result for a single scoring criterion."""

    criteriaType: CriteriaType
    aiScore: float = Field(ge=0, le=100)
    maxScore: float = Field(default=100.0)
    weightedScore: Optional[float] = None
    aiExplanation: Optional[str] = None
    hardSkills: List[HardSkillResult] = Field(default_factory=list)
    softSkills: List[SoftSkillResult] = Field(default_factory=list)
    experienceDetails: List[ExperienceDetailResult] = Field(default_factory=list)


class GapResult(BaseModel):
    """A gap between JD requirements and CV capabilities."""

    gapType: GapType
    itemName: str
    description: Optional[str] = None
    impact: Optional[ImpactType] = None
    impactScore: Optional[float] = Field(default=None, ge=0, le=100)
    suggestion: Optional[str] = None


class WeaknessResult(BaseModel):
    """A weakness identified in the candidate's profile."""

    weaknessText: str
    suggestion: Optional[str] = None
    context: Optional[str] = None
    criterionType: Optional[CriteriaType] = None
    severity: Optional[int] = Field(default=None, ge=1, le=5)


class MatchingResult(BaseModel):
    """Root response model for matching analysis."""

    aiOverallScore: float = Field(ge=0, le=100)
    matchLevel: MatchLevel
    summary: Optional[str] = None
    strengths: Optional[str] = None
    weakness: Optional[str] = None
    isTrueLevel: Optional[bool] = None
    hasRelatedExperience: Optional[bool] = None
    isSpecificJd: Optional[bool] = None
    aiModelVersion: Optional[str] = None
    criteriaScores: List[CriteriaScoreResult] = Field(default_factory=list)
    gaps: List[GapResult] = Field(default_factory=list)
    weaknesses: List[WeaknessResult] = Field(default_factory=list)
