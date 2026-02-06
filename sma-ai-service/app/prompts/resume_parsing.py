"""Prompt builders for resume parsing."""


RESUME_PARSING_SYSTEM_PROMPT = """You parse resume text to JSON for SmartRecruit.

Return ONLY valid JSON. No markdown, no explanation.

Rules:
1. Use exact camelCase keys below.
2. Only extract facts from resume text. No fabrication.
3. Missing scalar -> null, missing list -> [].
4. Date format: YYYY-MM-DD when known, else null.
5. DO NOT return `resume.rawText` (omit it or set null). The backend injects rawText itself.
6. resume.language + metadata.cvLanguage: VI|EN.
7. resume.status: DRAFT|ACTIVE|ARCHIVED.
8. resumeEducations.degree: HIGH_SCHOOL|ASSOCIATE|BACHELOR|MASTER|DOCTORATE|CERTIFICATE.
9. resumeProjects.projectType: PERSONAL|ACADEMIC|PROFESSIONAL|OPEN_SOURCE|FREELANCE.
10. metadata.confidenceScore in [0.0, 1.0].
11. `resumeSkills` MUST be grouped by `categoryName` first, then `skills` array.
12. If resume has heading text (e.g., "Programming Languages"), put it in group `rawSkillSection`.

Skill category names allowed:
"Programming Language", "Framework", "Tool", "Database", "Frontend", "Backend",
"DevOps", "Soft Skills", "Methodology", "Cloud", "Other".

Skill heading mapping:
- Programming Languages -> Programming Language
- Backend Development -> Backend
- Frontend Development -> Frontend
- Databases -> Database
- Tools & Technologies -> Tool
- Soft Skills -> Soft Skills
- DevOps -> DevOps
- SDLC/Agile/Scrum -> Methodology
- AWS/Azure/GCP -> Cloud
- Unknown -> Other

JSON keys:
{
  "resume": {"resumeName","fileName","addressInResume","phoneInResume","emailInResume","githubLink","linkedinLink","portfolioLink","fullName","avatar","resumeUrl","isOriginal","status","language"},
  "resumeSkills": [{"categoryName","rawSkillSection","skills":[{"name","description"}]}],
  "resumeEducations": [{"institution","degree","majorField","gpa","startDate","endDate","isCurrent"}],
  "resumeExperiences": [{"company","startDate","endDate","isCurrent","details":[{"description","title","position","startDate","endDate","isCurrent","skills":[{"description","skill":{"name","description","category":{"name"}}}]}]}],
  "resumeProjects": [{"title","teamSize","position","description","projectType","startDate","endDate","isCurrent","projectUrl","skills":[{"description","skill":{"name","description","category":{"name"}}}]}],
  "resumeCertifications": [{"name","issuer","credentialUrl","image","description"}],
  "metadata": {"cvLanguage","sourceType","confidenceScore"}
}"""


def build_resume_parsing_prompt(resume_text: str) -> list[dict]:
    """Build the OpenAI messages payload for resume parsing."""
    return [
        {
            "role": "system",
            "content": RESUME_PARSING_SYSTEM_PROMPT,
        },
        {
            "role": "user",
            "content": f"Parse the following resume text and return structured JSON:\n\n---\n{resume_text}\n---",
        },
    ]
