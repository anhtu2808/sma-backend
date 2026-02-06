"""Pydantic schemas aligned with core-service resume entities."""

from datetime import datetime
from typing import List, Literal, Optional

from pydantic import BaseModel, Field

DegreeType = Literal["HIGH_SCHOOL", "ASSOCIATE", "BACHELOR", "MASTER", "DOCTORATE", "CERTIFICATE"]
ProjectType = Literal["PERSONAL", "ACADEMIC", "PROFESSIONAL", "OPEN_SOURCE", "FREELANCE"]
ResumeStatus = Literal["DRAFT", "ACTIVE", "ARCHIVED"]
ResumeLanguage = Literal["VI", "EN"]
SkillCategoryName = Literal[
    "Programming Language",
    "Framework",
    "Tool",
    "Database",
    "Frontend",
    "Backend",
    "DevOps",
    "Soft Skills",
    "Methodology",
    "Cloud",
    "Other",
]


class SkillCategory(BaseModel):
    """Maps to skill_categories entity."""

    name: SkillCategoryName


class Skill(BaseModel):
    """Maps to skills entity."""

    name: str
    description: Optional[str] = None
    category: Optional[SkillCategory] = None


class Resume(BaseModel):
    """Maps to resumes entity fields."""

    resumeName: Optional[str] = None
    fileName: Optional[str] = None
    rawText: Optional[str] = None
    addressInResume: Optional[str] = None
    phoneInResume: Optional[str] = None
    emailInResume: Optional[str] = None
    githubLink: Optional[str] = None
    linkedinLink: Optional[str] = None
    portfolioLink: Optional[str] = None
    fullName: str
    avatar: Optional[str] = None
    resumeUrl: Optional[str] = None
    isOriginal: Optional[bool] = True
    status: Optional[ResumeStatus] = "ACTIVE"
    language: Optional[ResumeLanguage] = None


class ResumeSkill(BaseModel):
    """Maps to resume_skills entity."""

    rawSkillSection: Optional[str] = None
    skill: Skill


class ResumeEducation(BaseModel):
    """Maps to resume_educations entity."""

    institution: str
    degree: Optional[DegreeType] = None
    majorField: Optional[str] = None
    gpa: Optional[float] = None
    startDate: Optional[str] = None  # YYYY-MM-DD
    endDate: Optional[str] = None  # YYYY-MM-DD
    isCurrent: Optional[bool] = False


class ExperienceSkill(BaseModel):
    """Maps to experience_skills entity payload."""

    description: Optional[str] = None
    skill: Skill


class ResumeExperienceDetail(BaseModel):
    """Maps to resume_experience_details entity."""

    description: Optional[str] = None
    title: Optional[str] = None
    position: Optional[str] = None
    startDate: Optional[str] = None  # YYYY-MM-DD
    endDate: Optional[str] = None  # YYYY-MM-DD
    isCurrent: Optional[bool] = False
    skills: List[ExperienceSkill] = Field(default_factory=list)


class ResumeExperience(BaseModel):
    """Maps to resume_experiences entity."""

    company: str
    startDate: Optional[str] = None  # YYYY-MM-DD
    endDate: Optional[str] = None  # YYYY-MM-DD
    isCurrent: Optional[bool] = False
    details: List[ResumeExperienceDetail] = Field(default_factory=list)


class ProjectSkill(BaseModel):
    """Maps to project_skills entity payload."""

    description: Optional[str] = None
    skill: Skill


class ResumeProject(BaseModel):
    """Maps to resume_projects entity."""

    title: str
    teamSize: Optional[int] = None
    position: Optional[str] = None
    description: Optional[str] = None
    projectType: Optional[ProjectType] = None
    startDate: Optional[str] = None  # YYYY-MM-DD
    endDate: Optional[str] = None  # YYYY-MM-DD
    isCurrent: Optional[bool] = False
    projectUrl: Optional[str] = None
    skills: List[ProjectSkill] = Field(default_factory=list)


class ResumeCertification(BaseModel):
    """Maps to resume_certifications entity."""

    name: str
    issuer: Optional[str] = None
    credentialUrl: Optional[str] = None
    image: Optional[str] = None
    description: Optional[str] = None


class TokenUsage(BaseModel):
    """Token usage statistics."""

    promptTokens: int
    completionTokens: int
    totalTokens: int


class ParsedCVMetadata(BaseModel):
    """Metadata about the parsing process."""

    cvLanguage: Optional[ResumeLanguage] = None
    sourceType: str = "pdf"
    confidenceScore: Optional[float] = None
    parsedBy: str = "gpt-4.1"
    parsedAt: datetime = Field(default_factory=datetime.utcnow)
    usage: Optional[TokenUsage] = None
    costUsd: Optional[float] = None


class ParsedCV(BaseModel):
    """Root response model aligned with resume-related entities."""

    resume: Resume
    resumeSkills: List[ResumeSkill] = Field(default_factory=list)
    resumeEducations: List[ResumeEducation] = Field(default_factory=list)
    resumeExperiences: List[ResumeExperience] = Field(default_factory=list)
    resumeProjects: List[ResumeProject] = Field(default_factory=list)
    resumeCertifications: List[ResumeCertification] = Field(default_factory=list)
    metadata: ParsedCVMetadata
