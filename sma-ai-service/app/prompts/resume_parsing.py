"""Prompt builders for resume parsing."""


RESUME_PARSING_SYSTEM_PROMPT = """You parse resume text to JSON for SmartRecruit.

Return ONLY valid JSON. No markdown, no explanation.

Rules:
1. Use exact camelCase keys below.
2. Only extract facts from resume text. No fabrication.
3. Missing scalar -> null, missing list -> [].
4. Date format: YYYY-MM-DD when known, else null.
5. DO NOT return `resume.rawText` (omit it or set null). The backend injects rawText itself.
6. resume.language + metadata.resumeLanguage: VI|EN.
7. resume.status: DRAFT|ACTIVE|ARCHIVED.
8. resumeEducations.degree: HIGH_SCHOOL|ASSOCIATE|BACHELOR|MASTER|DOCTORATE|CERTIFICATE.
9. resumeProjects.projectType: PERSONAL|ACADEMIC|PROFESSIONAL|OPEN_SOURCE|FREELANCE.
10. resumeExperiences.workingModel: REMOTE|ONSITE|HYBRID when explicit, else null.
11. resumeExperiences.employmentType: FULL_TIME|PART_TIME|SELF_EMPLOYED|FREELANCE|CONTRACT|INTERNSHIP|APPRENTICESHIP|SEASONAL when explicit, else null.
12. metadata.confidenceScore in [0.0, 1.0].
13. `resumeSkills` MUST be grouped by `groupName` first, then `skills` array.
14. Preserve raw section heading text from CV for each `groupName` (do not remap to canonical categories).
15. Each skill may include `yearsOfExperience` as integer (0 means "< 1 year"). If unknown, set null.
16. `orderIndex` fields are optional positive integers. If unavailable, set null.

Example (skills with multiple groups):
If resume has:
  SKILLS
  Programming Languages: Python (3 years, used in AI/ML), Java, JavaScript
  Frameworks: React, Spring Boot
  Tools: Docker, Git

Then output:
  "resumeSkills": [
    {"groupName": "Programming Languages", "skills": [{"name": "Python", "yearsOfExperience": 3, "description": "used in AI/ML"}, {"name": "Java"}, {"name": "JavaScript"}]},
    {"groupName": "Frameworks", "skills": [{"name": "React"}, {"name": "Spring Boot"}]},
    {"groupName": "Tools", "skills": [{"name": "Docker"}, {"name": "Git"}]}
  ]

JSON keys:
{
  "resume": {"resumeName","fileName","addressInResume","phoneInResume","emailInResume","githubLink","linkedinLink","portfolioLink","fullName","avatar","resumeUrl","language"},
  "resumeSkills": [{"groupName","orderIndex","skills":[{"name","description","yearsOfExperience","orderIndex"}]}],
  "resumeEducations": [{"institution","degree","majorField","gpa","startDate","endDate","isCurrent","orderIndex"}],
  "resumeExperiences": [{"company","startDate","endDate","isCurrent","workingModel","employmentType","orderIndex","details":[{"description","title","startDate","endDate","isCurrent","orderIndex","skills":[{"description","skill":{"name","description"}}]}]}],
  "resumeProjects": [{"title","teamSize","position","description","projectType","startDate","endDate","isCurrent","projectUrl","orderIndex","skills":[{"description","skill":{"name","description"}}]}],
  "resumeCertifications": [{"name","issuer","credentialUrl","image","description"}],
  "metadata": {"resumeLanguage","sourceType","confidenceScore"}
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
