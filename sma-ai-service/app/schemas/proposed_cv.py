from enum import Enum
from pydantic import BaseModel, ConfigDict
from typing import List, Optional


class BaseSchema(BaseModel):
    model_config = ConfigDict(extra="ignore", str_strip_whitespace=True)


class ProposeStatus(str, Enum):
    NONE = "NONE"
    PROCESSING = "PROCESSING"
    FAILED = "FAILED"
    FINISHED = "FINISHED"


class ProposedCVRequestMessage(BaseSchema):
    id: int 


class ProposedCVData(BaseSchema):
    resumeId: int = None
    matchRate: float = None


class ProposedCVResultMessage(BaseSchema):
    status: ProposeStatus
    errorMessage: Optional[str] = None
    jobId: int
    proposedCVs: List[ProposedCVData] = None
