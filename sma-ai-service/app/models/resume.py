"""
Pydantic models for CV/Resume parsing.
Follows the canonical schema defined in the feature plan.
"""

from pydantic import BaseModel, Field, EmailStr
from typing import Optional, List
from datetime import datetime


class Resume(BaseModel):
    """Personal information section of the CV."""
    full_name: str
    job_title: Optional[str] = None
    date_of_birth: Optional[str] = None
    avatar: Optional[str] = None
    gender: Optional[str] = None
    address: Optional[str] = None
    phone: Optional[str] = None
    email: Optional[str] = None
    github_url: Optional[str] = None
    linkedin_url: Optional[str] = None
    website_url: Optional[str] = None
    reference_content: Optional[str] = None


class Skill(BaseModel):
    """Individual skill entry."""
    name: str
    level: Optional[str] = None  # e.g., "BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT"
    years_of_experience: Optional[float] = None


class Education(BaseModel):
    """Education history entry."""
    institution: str
    degree: Optional[str] = None
    major: Optional[str] = None
    start_year: Optional[int] = None
    end_year: Optional[int] = None
    gpa: Optional[float] = None


class ExperienceDetail(BaseModel):
    """Details of work experience tasks/achievements."""
    description: str
    skills: List[str] = Field(default_factory=list)


class Experience(BaseModel):
    """Work experience entry."""
    company_name: str
    position: str
    start_date: Optional[str] = None  # Format: "YYYY-MM" or "YYYY"
    end_date: Optional[str] = None    # Format: "YYYY-MM" or "YYYY"
    is_current: bool = False
    details: List[ExperienceDetail] = Field(default_factory=list)


class Project(BaseModel):
    """Project portfolio entry."""
    name: str
    role: Optional[str] = None
    description: Optional[str] = None
    technologies: List[str] = Field(default_factory=list)
    link: Optional[str] = None


class Certification(BaseModel):
    """Certification entry."""
    name: str
    issuer: Optional[str] = None
    issue_year: Optional[int] = None


class TokenUsage(BaseModel):
    """Token usage statistics."""
    prompt_tokens: int
    completion_tokens: int
    total_tokens: int


class ParsedCVMetadata(BaseModel):
    """Metadata about the parsing process."""
    cv_language: Optional[str] = None
    source_type: str = "pdf"
    confidence_score: Optional[float] = None
    parsed_by: str = "gpt-4.1"
    parsed_at: datetime = Field(default_factory=datetime.utcnow)
    usage: Optional[TokenUsage] = None
    cost_usd: Optional[float] = None  # Estimated cost in USD


class ParsedCV(BaseModel):
    """Root response model for parsed CV data."""
    resume: Resume
    skills: List[Skill] = Field(default_factory=list)
    educations: List[Education] = Field(default_factory=list)
    experiences: List[Experience] = Field(default_factory=list)
    projects: List[Project] = Field(default_factory=list)
    certifications: List[Certification] = Field(default_factory=list)
    metadata: ParsedCVMetadata
