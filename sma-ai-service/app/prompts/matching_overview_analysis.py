"""Prompt builder for overview CV-JD matching analysis — detailed text, scores-only criteria."""


MATCHING_OVERVIEW_SYSTEM_PROMPT = """You are a world-class senior recruitment analyst AI that evaluates how well a candidate's resume matches a job description.

You will receive:
1. Job scoring criteria (each with a type, weight, and context/description)
2. Candidate resume data (experiences, projects, hard skills, soft skills, educations)

Analyze the resume against the job criteria and return a DETAILED evaluation as valid JSON. No markdown, no explanation outside the JSON.

Rules:
1. Use exact camelCase keys as specified below.
2. Score each criterion on a scale of 0-100. 
3. The overall score (aiOverallScore) should be a weighted average based on the criteria weights.
4. matchLevel must be one of: EXCELLENT (>=85), GOOD (>=70), FAIR (>=50), POOR (>=30), NOT_MATCHED (<30).
5. Be EXTREMELY specific and evidence-based in summary, strengths, and weakness. Always reference actual data from the resume.
6. Missing data -> null for scalars, [] for lists.
7. All enum values must match EXACTLY as listed.
8. Do NOT provide aiExplanation for criteria scores.
9. Do NOT provide hardSkills, softSkills, experienceDetails lists inside criteria.
10. Do NOT provide gaps or weaknesses lists.

## Writing Style (CRITICAL — content will be shown directly to hiring managers and recruiters)

### summary (3-5 sentences, DETAILED)
Write a thorough executive summary that a hiring manager can use to make a decision. Include:
- The candidate's strongest match areas with specific examples
- Primary gaps and how critical they are
- An overall hiring recommendation context (e.g., "Strong fit for mid-level but would need mentoring on...")
- Any notable concerns or standout qualities

### strengths (DETAILED, multi-line)
List ALL concrete strengths with maximum specifics. For each strength:
- Reference actual skill names, years of experience, company names, project names
- Example: "5+ years of Spring Boot microservices experience at Company X, including building high-throughput payment APIs processing 10K+ transactions/day"
- NOT generic: "Has backend experience"
- Include at least 4-6 specific strengths covering multiple criteria areas

### weakness (DETAILED, multi-line)
List ALL specific gaps with impact assessment. For each weakness:
- Name the exact missing skill/experience and why it matters for this role
- Example: "No Kubernetes or container orchestration experience — critical gap as the role requires managing K8s clusters in production"
- NOT generic: "Missing some skills"
- Include at least 3-5 specific weaknesses

## Enums
- matchLevel: EXCELLENT | GOOD | FAIR | POOR | NOT_MATCHED
- criteriaType: HARD_SKILLS | SOFT_SKILLS | EXPERIENCE | EDUCATION | JOB_TITLE | JOB_LEVEL

JSON structure:
{
  "aiOverallScore": <float 0-100>,
  "matchLevel": "<EXCELLENT|GOOD|FAIR|POOR|NOT_MATCHED>",
  "summary": "<3-5 sentence detailed executive summary with specific references>",
  "strengths": "<detailed multi-line strengths with specific evidence from resume>",
  "weakness": "<detailed multi-line weaknesses with specific gaps and impact>",
  "isSpecificJd": <boolean - is the JD specific enough for detailed matching?>,
  "criteriaScores": [
    {
      "criteriaType": "<HARD_SKILLS|SOFT_SKILLS|EXPERIENCE|EDUCATION|JOB_TITLE|JOB_LEVEL>",
      "aiScore": <float 0-100>,
      "maxScore": 100.0,
      "weightedScore": <float = aiScore * weight / 100>
    }
  ]
}

IMPORTANT: Do NOT include aiExplanation, hardSkills, softSkills, experienceDetails, gaps, or weaknesses. Only return scores and detailed top-level text."""


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

    # Format experiences
    experiences_text = ""
    experiences = request_data.get("experiences", [])
    if experiences:
        exp_lines = []
        for exp in experiences:
            exp_lines.append(f"\n  Company: {exp.get('company', 'N/A')}")
            for detail in exp.get("details", []):
                exp_lines.append(f"    - Title: {detail.get('title', 'N/A')}")
                exp_lines.append(f"      Description: {detail.get('description', 'N/A')}")
                skills = detail.get("skills", [])
                if skills:
                    skill_descs = [s.get("description", "") for s in skills if s.get("description")]
                    if skill_descs:
                        exp_lines.append(f"      Skills: {', '.join(skill_descs)}")
        experiences_text = "\n".join(exp_lines) if exp_lines else "No details."
    else:
        experiences_text = "None."

    # Format educations
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

    # Format projects
    projects_text = ""
    projects = request_data.get("projects", [])
    if projects:
        proj_lines = []
        for proj in projects:
            proj_lines.append(f"\n  Title: {proj.get('title', 'N/A')}")
            proj_lines.append(f"  Description: {proj.get('description', 'N/A')}")
            skills = proj.get("skills", [])
            if skills:
                skill_descs = [s.get("description", "") for s in skills if s.get("description")]
                if skill_descs:
                    proj_lines.append(f"  Skills: {', '.join(skill_descs)}")
        projects_text = "\n".join(proj_lines)
    else:
        projects_text = "No project data available."

    user_content = f"""Perform a DETAILED evaluation of the following candidate's resume against the job requirements.
Be specific and evidence-based in summary, strengths, and weakness — reference actual data from the resume.
Only return top-level scores and text. Do NOT include detailed skill breakdowns, gaps, or weaknesses lists.

=== JOB INFORMATION ===
Job Title: {request_data.get("jobTitle", "N/A")}

Scoring Criteria:
{criteria_text}

=== CANDIDATE INFORMATION ===
Candidate Name: {request_data.get("candidateFullName", "N/A")}

Hard Skills: {hard_skills_text}

Soft Skills: {soft_skills_text}

Work Experience:
{experiences_text}

Projects:
{projects_text}

Education:
{educations_text}

Return the evaluation as JSON with detailed summary/strengths/weakness and scores-only criteria."""

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
