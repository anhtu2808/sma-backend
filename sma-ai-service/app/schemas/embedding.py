from enum import Enum
from pydantic import BaseModel, Field, ConfigDict
from typing import List, Optional


class BaseSchema(BaseModel):
    model_config = ConfigDict(extra="ignore", str_strip_whitespace=True)


class EmbedStatus(str, Enum):
    NONE = "NONE"
    SUCCESS = "SUCCESS"
    FAIL = "FAIL"


class EmbeddingResumeSkill(BaseSchema):
    name: Optional[str] = None
    yearsOfExperience: Optional[int] = None


class EmbeddingResumeEducation(BaseSchema):
    institution: Optional[str] = None
    degree: Optional[str] = None
    majorField: Optional[str] = None


class EmbeddingExperienceSkill(BaseSchema):
    name: Optional[str] = None
    description: Optional[str] = None


class EmbeddingExperienceDetail(BaseSchema):
    title: Optional[str] = None
    description: Optional[str] = None
    experienceSkills: List[EmbeddingExperienceSkill] = Field(default_factory=list)


class EmbeddingResumeExperience(BaseSchema):
    company: Optional[str] = None
    workingModel: Optional[str] = None
    employmentType: Optional[str] = None
    details: List[EmbeddingExperienceDetail] = Field(default_factory=list)


class EmbeddingProjectSkill(BaseSchema):
    name: Optional[str] = None
    description: Optional[str] = None


class EmbeddingResumeProject(BaseSchema):
    title: Optional[str] = None
    position: Optional[str] = None
    description: Optional[str] = None
    projectSkills: List[EmbeddingProjectSkill] = Field(default_factory=list)


class EmbeddingResumeRequestMessage(BaseSchema):
    id: int
    address: Optional[str] = None

    skills: List[EmbeddingResumeSkill] = Field(default_factory=list)
    educations: List[EmbeddingResumeEducation] = Field(default_factory=list)
    experiences: List[EmbeddingResumeExperience] = Field(default_factory=list)
    projects: List[EmbeddingResumeProject] = Field(default_factory=list)


class EmbeddingResumeResultMessage(BaseSchema):
    id: int
    status: EmbedStatus
    errorMessage: Optional[str] = None


class EmbeddingJobSkill(BaseSchema):
    name: Optional[str] = None


class EmbeddingJobRequestMessage(BaseSchema):
    id: int
    title: Optional[str] = None
    about: Optional[str] = None
    responsibilities: Optional[str] = None
    requirement: Optional[str] = None
    jobLevel: Optional[str] = None
    expertiseName: Optional[str] = None
    skills: List[EmbeddingJobSkill] = Field(default_factory=list)


class EmbeddingJobResultMessage(BaseSchema):
    id: int
    status: EmbedStatus
    errorMessage: Optional[str] = None