from pydantic import BaseModel
from typing import List, Optional

class GapSuggestionRequest(BaseModel):
    id: int
    gapType: Optional[str] = None
    itemName: Optional[str] = None
    description: Optional[str] = None
    impact: Optional[str] = None

class WeaknessSuggestionRequest(BaseModel):
    id: int
    weaknessText: Optional[str] = None
    context: Optional[str] = None
    severity: Optional[int] = None
    criterionType: Optional[str] = None
    suggestion: Optional[str] = None  # Existing suggestion, if any

class ReSuggestRequestMessage(BaseModel):
    jobId: Optional[int] = None
    resumeId: Optional[int] = None
    evaluationId: Optional[int] = None
    jobName: Optional[str] = None
    jobLevel: Optional[str] = None
    isTrueLevel: Optional[bool] = None
    hasRelatedExperience: Optional[bool] = None
    matchLevel: Optional[str] = None
    summary: Optional[str] = None
    weakness: Optional[WeaknessSuggestionRequest] = None

class SuggestionRequestMessage(BaseModel):
    jobId: Optional[int] = None
    resumeId: Optional[int] = None
    evaluationId: Optional[int] = None
    jobName: Optional[str] = None
    jobLevel: Optional[str] = None
    gaps: List[GapSuggestionRequest] = []
    weaknesses: List[WeaknessSuggestionRequest] = []
    isTrueLevel: Optional[bool] = None
    hasRelatedExperience: Optional[bool] = None
    matchLevel: Optional[str] = None
    summary: Optional[str] = None

class GapSuggestionResponse(BaseModel):
    id: int
    suggestion: str

class WeaknessSuggestionResponse(BaseModel):
    id: int
    suggestion: str

class SuggestResultMessage(BaseModel):
    evaluationId: int
    status: str = "SUCCESS"  # "SUCCESS" or "ERROR"
    errorMessage: Optional[str] = None
    weaknessSuggestion: List[WeaknessSuggestionResponse] = []
    gapSuggestion: List[GapSuggestionResponse] = []
