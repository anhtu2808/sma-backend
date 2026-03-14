"""Pydantic schemas for detail matching analysis — adds aiExplanation, detail breakdowns, and suggestions."""

from typing import List, Literal, Optional

from pydantic import BaseModel, Field, model_validator


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

    @model_validator(mode="after")
    def validate_status_specific_fields(self) -> "CriteriaScoreDetailResult":
        """Keep detail payloads consistent with downstream UI expectations."""
        context = (self.context or "").strip()
        suggestions = [item.strip() for item in self.suggestions if item and item.strip()]

        if self.status == "MISSING":
            if not context:
                raise ValueError("MISSING items must include a non-empty context.")
            if self.impactScore is None:
                raise ValueError("MISSING items must include impactScore.")
            if not suggestions:
                raise ValueError("MISSING items must include at least one suggestion.")
        else:
            if self.impactScore is not None:
                raise ValueError("MATCHED items must not include impactScore.")
            if suggestions:
                raise ValueError("MATCHED items must have an empty suggestions list.")

        self.suggestions = suggestions
        self.context = context or None
        return self


class CriteriaScoreResult(BaseModel):
    """Evaluation result for a single scoring criterion with detail breakdowns."""

    id: int
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
