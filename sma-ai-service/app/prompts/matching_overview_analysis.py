"""Prompt builder for overview CV-JD matching analysis — fast scoring without deep details."""


MATCHING_OVERVIEW_SYSTEM_PROMPT = """You are an expert recruitment AI that quickly evaluates how well a candidate's resume matches a job description.

You will receive:
1. Job scoring criteria (each with a type, weight, and context/description)
2. Candidate resume data (experiences, projects, hard skills, soft skills, educations)

Provide a QUICK, CONCISE scoring evaluation as valid JSON. No markdown, no explanation outside the JSON.

Rules:
1. Use exact camelCase keys as specified below.
2. Score each criterion on a scale of 0-100.
3. The overall score (aiOverallScore) should be a weighted average based on the criteria weights.
4. matchLevel must be one of: EXCELLENT (>=85), GOOD (>=70), FAIR (>=50), POOR (>=30), NOT_MATCHED (<30).
5. Keep summary, strengths, and weakness SHORT (1-2 sentences each). Be direct and specific.
6. Do NOT provide explanations for individual criteria scores.
7. Do NOT provide hardSkills, softSkills, experienceDetails, gaps, or weaknesses lists.
8. Missing data -> null for scalars, [] for lists.
9. All enum values must match EXACTLY as listed.

## Writing Style
- summary: 1-2 sentences max. Mention the strongest match and biggest gap concisely.
- strengths: Comma-separated list of 3-5 key strengths. Be specific (e.g., "5 years Spring Boot" not "backend experience").
- weakness: Comma-separated list of 2-4 key gaps. Be specific (e.g., "No Kubernetes experience" not "missing skills").
- matchLevel: EXCELLENT | GOOD | FAIR | POOR | NOT_MATCHED
- criteriaType: HARD_SKILLS | SOFT_SKILLS | EXPERIENCE | EDUCATION | JOB_TITLE | JOB_LEVEL

JSON structure:
{
  "aiOverallScore": <float 0-100>,
  "matchLevel": "<EXCELLENT|GOOD|FAIR|POOR|NOT_MATCHED>",
  "summary": "<1-2 sentence quick assessment>",
  "strengths": "<comma-separated key strengths>",
  "weakness": "<comma-separated key weaknesses>",
  "isTrueLevel": <boolean>,
  "hasRelatedExperience": <boolean>,
  "isSpecificJd": <boolean>,
  "criteriaScores": [
    {
      "criteriaType": "<HARD_SKILLS|SOFT_SKILLS|EXPERIENCE|EDUCATION|JOB_TITLE|JOB_LEVEL>",
      "aiScore": <float 0-100>,
      "maxScore": 100.0,
      "weightedScore": <float = aiScore * weight / 100>
    }
  ]
}

IMPORTANT: Keep token usage LOW. Only return scores and brief text. No detailed explanations."""


def build_matching_overview_prompt(request_data: dict) -> list[dict]:
    """Build the OpenAI messages payload for overview matching analysis.

    Args:
        request_data: The matching request data containing job criteria and resume data.

    Returns:
        List of message dicts for OpenAI chat completion.
    """
    # Format job criteria
    criteria_text = ""
    criteria = request_data.get("criteria", [])
    if criteria:
        criteria_lines = []
        for c in criteria:
            criteria_lines.append(
                f"- Type: {c.get('criteriaType', 'N/A')}, "
                f"Weight: {c.get('weight', 0)}%, "
                f"Context: {c.get('context', 'N/A')}"
            )
        criteria_text = "\n".join(criteria_lines)
    else:
        criteria_text = "No specific scoring criteria provided."

    # Format hard skills (concise)
    hard_skills_text = ""
    hard_skills = request_data.get("hardSkills", [])
    if hard_skills:
        hs_lines = []
        for hs in hard_skills:
            yoe = hs.get("yearsOfExperience")
            yoe_str = f" ({yoe}y)" if yoe else ""
            hs_lines.append(f"{hs.get('name', 'N/A')}{yoe_str}")
        hard_skills_text = ", ".join(hs_lines)
    else:
        hard_skills_text = "None listed."

    # Format soft skills (concise)
    soft_skills_text = ""
    soft_skills = request_data.get("softSkills", [])
    if soft_skills:
        soft_skills_text = ", ".join(ss.get("name", "N/A") for ss in soft_skills)
    else:
        soft_skills_text = "None listed."

    # Format experiences (concise)
    experiences_text = ""
    experiences = request_data.get("experiences", [])
    if experiences:
        exp_lines = []
        for exp in experiences:
            for detail in exp.get("details", []):
                exp_lines.append(
                    f"- {detail.get('title', 'N/A')} at {exp.get('company', 'N/A')}"
                )
        experiences_text = "\n".join(exp_lines) if exp_lines else "No details."
    else:
        experiences_text = "None."

    # Format educations (concise)
    educations_text = ""
    educations = request_data.get("educations", [])
    if educations:
        edu_lines = []
        for edu in educations:
            edu_lines.append(
                f"- {edu.get('degree', 'N/A')} in {edu.get('majorField', 'N/A')} at {edu.get('institution', 'N/A')}"
            )
        educations_text = "\n".join(edu_lines)
    else:
        educations_text = "None."

    user_content = f"""Score this candidate against the job requirements (overview only):

=== JOB ===
Title: {request_data.get("jobTitle", "N/A")}
Criteria:
{criteria_text}

=== CANDIDATE ===
Name: {request_data.get("candidateFullName", "N/A")}
Hard Skills: {hard_skills_text}
Soft Skills: {soft_skills_text}
Experience:
{experiences_text}
Education:
{educations_text}

Return JSON with scores only."""

    return [
        {
            "role": "system",
            "content": MATCHING_OVERVIEW_SYSTEM_PROMPT,
        },
        {
            "role": "user",
            "content": user_content,
        },
    ]
