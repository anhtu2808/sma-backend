"""Prompt builder for overview CV-JD matching analysis — uses raw resume text, scores-only criteria."""


MATCHING_OVERVIEW_SYSTEM_PROMPT = """You are a world-class senior recruitment analyst AI that evaluates how well a candidate's resume matches a job description.

You will receive:
1. Job scoring criteria (each with a type, weight, context/description, and optional scoring rule)
2. Candidate's raw resume text

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
9. Do NOT provide details list inside criteria.
10. If a scoring rule is provided for a criterion, you MUST follow that rule exactly to determine the score. This ensures consistency across multiple scoring sessions.

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
- transferabilityToRole: HIGH | MEDIUM | LOW

JSON structure:
{
  "aiOverallScore": <float 0-100>,
  "matchLevel": "<EXCELLENT|GOOD|FAIR|POOR|NOT_MATCHED>",
  "summary": "<3-5 sentence detailed executive summary with specific references>",
  "strengths": "<detailed multi-line strengths with specific evidence from resume>",
  "weakness": "<detailed multi-line weaknesses with specific gaps and impact>",
  "isTrueLevel": <boolean - does candidate genuinely meet the required job level?>,
  "hasRelatedExperience": <boolean - does candidate have directly relevant experience?>,
  "transferabilityToRole": "<HIGH|MEDIUM|LOW>",
  "criteriaScores": [
    {
      "criteriaType": "<HARD_SKILLS|SOFT_SKILLS|EXPERIENCE|EDUCATION|JOB_TITLE|JOB_LEVEL>",
      "aiScore": <float 0-100>
    }
  ]
}

IMPORTANT: Do NOT include aiExplanation or details inside criteriaScores. Only return scores and detailed top-level text."""


def build_matching_overview_prompt(request_data: dict) -> list[dict]:
    """Build the OpenAI messages payload for overview matching analysis.

    Args:
        request_data: The matching request data containing job criteria and raw resume text.

    Returns:
        List of message dicts for OpenAI chat completion.
    """
    # Format job criteria (with optional rule)
    criteria_text = ""
    criteria = request_data.get("criteria", [])
    if criteria:
        criteria_lines = []
        for c in criteria:
            line = (
                f"- Type: {c.get('criteriaType', 'N/A')}, "
                f"Weight: {c.get('weight', 0)}%, "
                f"Context: {c.get('context', 'N/A')}"
            )
            rule = c.get("rule")
            if rule:
                line += f", Scoring Rule: {rule}"
            criteria_lines.append(line)
        criteria_text = "\n".join(criteria_lines)
    else:
        criteria_text = "No specific scoring criteria provided."

    # Get raw resume text
    raw_resume_text = request_data.get("rawResumeText", "")
    if not raw_resume_text:
        raw_resume_text = "No resume text provided."

    user_content = f"""Perform a DETAILED evaluation of the following candidate's resume against the job requirements.
Be specific and evidence-based in summary, strengths, and weakness — reference actual data from the resume.
Only return top-level scores and text. Do NOT include detailed skill breakdowns or details lists.
If scoring rules are provided for criteria, follow them strictly for consistent scoring.

=== JOB INFORMATION ===
Job Title: {request_data.get("jobTitle", "N/A")}

Scoring Criteria:
{criteria_text}

=== CANDIDATE RESUME (RAW TEXT) ===
Candidate Name: {request_data.get("candidateFullName", "N/A")}

{raw_resume_text}

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
