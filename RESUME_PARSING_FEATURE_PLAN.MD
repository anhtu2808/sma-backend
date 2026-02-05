# CV Parsing AI Service (Stateless)

## 1. Overview

This service is a stateless AI-powered CV parsing API.

- Input: CV file (PDF)
- Processing:
  - Extract raw text from PDF
  - Parse structured data using GPT
- Output: Normalized JSON following a strict schema
- No database
- No state persistence
- No business logic
- Pure AI parsing responsibility

This service is designed to be consumed by a Java backend that handles persistence, scoring, and workflows.

---

## 2. Architecture

Client  
→ POST /v1/resume/parse (resume pdf link)  
→ Python API (FastAPI)  
→ PDF Loader  
→ Text Cleaner  
→ Prompt Builder  
→ GPT Client  
→ JSON Validator  
→ Structured JSON Response  

---

## 3. Responsibilities (Strict Scope)

### What this service DOES
- Read PDF CV
- Extract raw text
- Call GPT for semantic parsing
- Return structured JSON
- Validate schema before response

### What this service DOES NOT do
- Save data
- Connect to database
- Deduplicate skills
- Score candidates
- Apply business rules

---

## 4. API Design

### Endpoint

POST /v1/resume/parse  
Content-Type: multipart/form-data

### Request

Field: file  
Type: PDF  
Required: yes

---

## 5. JSON Response Schema (Canonical)

Root structure:

{
  "resume": {},
  "skills": [],
  "educations": [],
  "experiences": [],
  "projects": [],
  "certifications": [],
  "metadata": {}
}

---

## 6. Data Structures (Contract)

### 6.1 Resume

{
  "resume": {
    "full_name": "Nguyen Van A",
    "job_title": "Backend Developer",
    "date_of_birth": "1999-08-12",
    "avatar": null,
    "gender": "MALE",
    "address": "Ho Chi Minh City, Vietnam",
    "phone": "0909123456",
    "email": "vana@gmail.com",
    "github_url": "https://github.com/vanA",
    "linkedin_url": "https://linkedin.com/in/vanA",
    "website_url": null,
    "reference_content": "Available upon request"
  }
}

---

### 6.2 Skills

{
  "skills": [
    {
      "name": "Java",
      "level": "INTERMEDIATE",
      "years_of_experience": 2
    }
  ]
}

---

### 6.3 Educations

{
  "educations": [
    {
      "institution": "FPT University",
      "degree": "Bachelor",
      "major": "Software Engineering",
      "start_year": 2019,
      "end_year": 2023,
      "gpa": 3.2
    }
  ]
}

---

### 6.4 Experiences

{
  "experiences": [
    {
      "company_name": "ABC Tech",
      "position": "Backend Developer",
      "start_date": "2023-01",
      "end_date": "2024-06",
      "is_current": false,
      "details": [
        {
          "description": "Developed REST APIs using Spring Boot",
          "skills": ["Java", "Spring Boot"]
        }
      ]
    }
  ]
}

---

### 6.5 Projects

{
  "projects": [
    {
      "name": "Smart Recruit System",
      "role": "Backend Developer",
      "description": "AI-powered CV screening system",
      "technologies": ["Java", "Spring Boot"],
      "link": "https://github.com/..."
    }
  ]
}

---

### 6.6 Certifications

{
  "certifications": [
    {
      "name": "AWS Certified Cloud Practitioner",
      "issuer": "Amazon",
      "issue_year": 2024
    }
  ]
}

---

### 6.7 Metadata

{
  "metadata": {
    "cv_language": "en",
    "source_type": "pdf",
    "confidence_score": 0.92,
    "parsed_by": "gpt-4.1",
    "parsed_at": "2026-02-05T09:30:00Z"
  }
}

---

## 7. Python Models (Pydantic)

class Skill(BaseModel):
    name: str
    level: Optional[str]
    years_of_experience: Optional[float]

class ExperienceDetail(BaseModel):
    description: str
    skills: List[str]

class Experience(BaseModel):
    company_name: str
    position: str
    start_date: Optional[str]
    end_date: Optional[str]
    is_current: bool
    details: List[ExperienceDetail]

class Resume(BaseModel):
    full_name: str
    job_title: Optional[str]
    date_of_birth: Optional[str]
    gender: Optional[str]
    address: Optional[str]
    phone: Optional[str]
    email: Optional[str]
    github_url: Optional[str]
    linkedin_url: Optional[str]
    website_url: Optional[str]
    reference_content: Optional[str]

class ParsedCV(BaseModel):
    resume: Resume
    skills: List[Skill]
    educations: list
    experiences: List[Experience]
    projects: list
    certifications: list
    metadata: dict

---

## 8. Prompt Strategy

System prompt:

You are a CV parsing engine.
Return ONLY valid JSON.
Follow EXACT schema.
Do NOT invent data.
If information is missing, use null or empty array.
Dates must be ISO format.

User prompt:

Parse the following CV text and return structured JSON.

---

## 9. Error Handling

- Invalid PDF → 400
- GPT invalid JSON → 422
- Schema validation failed → 422
- GPT timeout → 504

---

## 10. Deployment Notes

- Stateless, horizontally scalable
- Suitable for Docker or AWS Lambda
- No shared memory
- No sticky session

---

## 11. Design Rationale

- Clear separation of concerns
- AI service is replaceable
- Backend controls data lifecycle
- Easy to test with mock PDFs
- Aligns with microservice and AI best practices