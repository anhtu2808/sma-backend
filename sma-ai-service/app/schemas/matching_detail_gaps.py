"""Pydantic schemas for detail supplement matching analysis — adds aiExplanation, nested skills, gaps & weaknesses."""

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


class CriteriaDetailResult(BaseModel):
    """Detail supplement for a single scoring criterion — adds explanation and nested skills."""

    criteriaType: CriteriaType
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


class WeaknessResult(BaseModel):
    """A weakness identified in the candidate's profile."""

    weaknessText: str
    context: Optional[str] = None
    criterionType: Optional[CriteriaType] = None
    severity: Optional[int] = Field(default=None, ge=1, le=5)


class MatchingDetailSupplementResult(BaseModel):
    """Response model for detail supplement — only the parts not covered by overview."""

    isTrueLevel: Optional[bool] = None
    hasRelatedExperience: Optional[bool] = None
    processingTimeSecond: Optional[float] = None
    criteriaScores: List[CriteriaDetailResult] = Field(default_factory=list)
    gaps: List[GapResult] = Field(default_factory=list)
    weaknesses: List[WeaknessResult] = Field(default_factory=list)
