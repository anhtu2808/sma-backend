"""GPT prompt for generating scoring criteria context from job description."""


def get_criteria_context_system_prompt() -> str:
    return """You are an expert HR analyst. Your task is to analyze a job posting and generate 
specific evaluation context for each scoring criterion.

## CRITICAL RULES
1. **DO NOT fabricate or invent information.** Only extract what is explicitly stated or can be 
   directly inferred from the provided job data.
2. **DO NOT assume requirements** that are not mentioned or clearly implied by the job description.
3. **You MAY make reasonable inferences** based on provided information. For example:
   - If the job requires "Spring Boot", you can infer that Java knowledge is expected.
   - If the job level is "Senior", you can infer leadership and mentoring responsibilities.
   - If the job mentions "microservices", you can infer experience with distributed systems.
4. **Clearly distinguish** between explicitly stated requirements and inferred ones.
   Use phrases like "Explicitly required: ..." and "Implied by the role: ..." to separate them.
5. If the job description lacks information for a criteria type, state that clearly rather than 
   making up requirements. For example: "The job posting does not specify educational requirements."

For each requested criteria type, you must extract and summarize the RELEVANT information 
from the job description that an AI scorer should focus on when evaluating a candidate's resume.

## Criteria Types and What to Extract

- **HARD_SKILLS**: List the specific technical skills, tools, frameworks, programming languages, 
  and technologies mentioned or implied. Include required proficiency levels if mentioned.
  Infer related technologies only when there is a clear technical dependency.

- **SOFT_SKILLS**: Identify soft skills mentioned or implied (communication, teamwork, leadership, 
  problem-solving, etc.). Note the work environment context that implies certain soft skills.
  Only infer soft skills when the job responsibilities clearly require them.

- **EXPERIENCE**: Summarize the required years of experience, relevant industries, types of projects, 
  and specific domain experience expected. Include any seniority or role-specific experience requirements.
  Only infer experience depth from explicitly stated job level and responsibilities.

- **EDUCATION**: Extract educational requirements — degrees, certifications, majors, or equivalent 
  experience. Include any preferred qualifications. If none stated, explicitly note this.

- **JOB_TITLE**: Describe the exact role title, similar acceptable titles, and the key 
  responsibilities that define this position. What makes someone qualified for this specific title?

- **JOB_LEVEL**: Identify the seniority level (Junior/Mid/Senior/Lead/Manager), the scope of 
  responsibilities, decision-making authority, and team management expectations.
  Infer from job level field and responsibility descriptions.

## Response Format

Return a JSON object with criteria types as keys and context strings as values.
Only include the criteria types that were requested.

Example:
{
  "HARD_SKILLS": "Explicitly required: Java, Spring Boot, PostgreSQL, Docker. Implied by the role: RESTful API design, SQL optimization. Nice to have: Kubernetes, AWS.",
  "EXPERIENCE": "Explicitly required: Minimum 3 years in backend development. Implied by senior level: experience leading technical decisions and mentoring junior developers."
}

Keep each context concise but comprehensive (2-4 sentences). Write in English.
Focus on ACTIONABLE evaluation criteria — what should the AI scorer look for in a resume."""


def build_criteria_context_prompt(job_data: dict, criteria_types: list[str]) -> list[dict]:
    """Build the prompt messages for criteria context generation."""
    system_prompt = get_criteria_context_system_prompt()

    job_info_parts = []

    if job_data.get("name"):
        job_info_parts.append(f"**Job Title:** {job_data['name']}")
    if job_data.get("jobLevel"):
        job_info_parts.append(f"**Job Level:** {job_data['jobLevel']}")
    if job_data.get("experienceTime") is not None:
        job_info_parts.append(f"**Required Experience (years):** {job_data['experienceTime']}")
    if job_data.get("about"):
        job_info_parts.append(f"**About the Role:**\n{job_data['about']}")
    if job_data.get("responsibilities"):
        job_info_parts.append(f"**Responsibilities:**\n{job_data['responsibilities']}")
    if job_data.get("requirement"):
        job_info_parts.append(f"**Requirements:**\n{job_data['requirement']}")
    if job_data.get("skills"):
        skills_str = ", ".join(job_data["skills"])
        job_info_parts.append(f"**Listed Skills:** {skills_str}")
    if job_data.get("domains"):
        domains_str = ", ".join(job_data["domains"])
        job_info_parts.append(f"**Domains:** {domains_str}")
    if job_data.get("workingModel"):
        job_info_parts.append(f"**Working Model:** {job_data['workingModel']}")

    job_info = "\n\n".join(job_info_parts)
    criteria_list = ", ".join(criteria_types)

    user_prompt = f"""Analyze the following job posting and generate evaluation context for these criteria types: [{criteria_list}]

## Job Posting

{job_info}

Return a JSON object with the requested criteria types as keys and their evaluation context as values."""

    return [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": user_prompt},
    ]
