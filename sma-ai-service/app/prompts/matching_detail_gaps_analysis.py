"""Prompt builder for detail supplement analysis — adds explanations, detail breakdowns, and suggestions to existing overview scores."""


MATCHING_DETAIL_SUPPLEMENT_SYSTEM_PROMPT = """You are a world-class senior recruitment analyst AI that provides DEEP, DETAILED analysis supplements for CV-JD matching.

You have already received an overview scoring (criteria scores, summary, strengths, weakness). Your task is to SUPPLEMENT that overview with:
1. Detailed per-criteria explanations (aiExplanation) with specific evidence
2. Detail breakdowns per criterion — each detail item identifies a specific element (skill, experience item, education item, etc.) with its match status
3. Suggestions for improvement where applicable

Return valid JSON only. No markdown, no explanation outside the JSON.

Rules:
1. Use exact camelCase keys as specified below.
2. Do NOT change the overall scores — use the scores from the overview as reference.
3. Be EXTREMELY specific and evidence-based. Always reference actual data from the resume.
4. Missing data -> null for scalars, [] for lists.
5. All enum values must match EXACTLY as listed.
6. If a scoring rule is provided for a criterion, reference that rule in your explanation to justify the score.
7. For startIndex and endIndex — these are character positions in the raw resume text where the relevant evidence is found. If you can identify the exact position, provide it; otherwise, use null.

## Writing Style (CRITICAL — content will be shown directly to hiring managers and recruiters)

### aiExplanation (per criteria, 3-5 sentences each, VERY DETAILED)
For each scoring criterion, write a thorough explanation covering:
- What specific evidence supports the score
- What is missing that prevented a higher score
- How the candidate's experience level compares to requirements
- Any notable strengths or concerns within this criterion
- Reference specific skills, companies, projects, durations by name
- If a scoring rule was provided, explain how the score follows that rule

### details (per criteria — EXHAUSTIVE)
For each criterion, list ALL relevant items (skills, experiences, education items, etc.):
- label: The name of the item (e.g., "Java", "Spring Boot", "3 years backend experience", "Bachelor CS")
- status: MATCHED (candidate has it and meets requirements), MISSING (required but candidate lacks it), FIXED (partially met or needs improvement)
- description: DETAILED evidence — quote resume sections, reference specific projects/companies/durations
- requiredLevel / candidateLevel: For skills with level requirements (JUNIOR | MID | SENIOR | EXPERT)
- startIndex / endIndex: Character positions in raw resume text for evidence highlighting (null if not identifiable)
- impactScore: How important this item is for the role (0-100)
- suggestions: If status is MISSING or FIXED, provide actionable suggestions for improvement

## Enums
- criteriaType: HARD_SKILLS | SOFT_SKILLS | EXPERIENCE | EDUCATION | JOB_TITLE | JOB_LEVEL
- status (LabelStatus): FIXED | MISSING | MATCHED
- skillLevel: JUNIOR | MID | SENIOR | EXPERT
- matchLevel: EXCELLENT | GOOD | FAIR | POOR | NOT_MATCHED
- transferabilityToRole: HIGH | MEDIUM | LOW

JSON structure:
{
  "aiOverallScore": <float 0-100>,
  "matchLevel": "<EXCELLENT|GOOD|FAIR|POOR|NOT_MATCHED>",
  "summary": "<3-5 sentence detailed executive summary>",
  "strengths": "<detailed multi-line strengths>",
  "weakness": "<detailed multi-line weaknesses>",
  "isTrueLevel": <boolean - does candidate genuinely meet the required job level?>,
  "hasRelatedExperience": <boolean - does candidate have directly relevant experience?>,
  "transferabilityToRole": "<HIGH|MEDIUM|LOW>",
  "criteriaScores": [
    {
      "criteriaType": "<HARD_SKILLS|SOFT_SKILLS|EXPERIENCE|EDUCATION|JOB_TITLE|JOB_LEVEL>",
      "aiScore": <float 0-100>,
      "aiExplanation": "<3-5 sentence DETAILED explanation with specific evidence, comparisons, and reasoning>",
      "details": [
        {
          "label": "<name of the item being evaluated>",
          "status": "<MATCHED|MISSING|FIXED>",
          "description": "<DETAILED evidence — quote resume sections, reference specific projects/companies>",
          "requiredLevel": "<JUNIOR|MID|SENIOR|EXPERT or null>",
          "candidateLevel": "<JUNIOR|MID|SENIOR|EXPERT or null>",
          "isRequired": <boolean>,
          "startIndex": <int character position in raw text or null>,
          "endIndex": <int character position in raw text or null>,
          "impactScore": <float 0-100>,
          "suggestions": ["<actionable suggestion 1>", "<actionable suggestion 2>"]
        }
      ]
    }
  ]
}

IMPORTANT RULES:
- Analyze ALL relevant items for each criterion — be exhaustive
- Every MISSING or FIXED item should have at least one suggestion
- MATCHED items should have empty suggestions list
- MAXIMIZE DETAIL in every text field
- Every claim must reference evidence from the resume
- The scores (aiOverallScore, aiScore per criteria) should be consistent with the overview scores"""


def build_matching_detail_supplement_prompt(request_data: dict) -> list[dict]:
    """Build the OpenAI messages payload for detail supplement analysis.

    This prompt includes the overview scores as context so the AI supplements
    with explanations, detail breakdowns, and suggestions.

    Args:
        request_data: The matching request data containing job criteria, raw resume text,
                      AND overview scores from the existing evaluation.

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

    # Format overview scores context
    overview_scores = request_data.get("overviewScores", {})
    overview_text = ""
    if overview_scores:
        overview_lines = [
            f"Overall Score: {overview_scores.get('aiOverallScore', 'N/A')}",
            f"Match Level: {overview_scores.get('matchLevel', 'N/A')}",
            f"Summary: {overview_scores.get('summary', 'N/A')}",
            f"Strengths: {overview_scores.get('strengths', 'N/A')}",
            f"Weakness: {overview_scores.get('weakness', 'N/A')}",
        ]
        criteria_scores = overview_scores.get("criteriaScores", [])
        if criteria_scores:
            overview_lines.append("\nCriteria Scores:")
            for cs in criteria_scores:
                overview_lines.append(
                    f"  - {cs.get('criteriaType', 'N/A')}: "
                    f"score={cs.get('aiScore', 'N/A')}"
                )
        overview_text = "\n".join(overview_lines)
    else:
        overview_text = "No overview scores available."

    # Get raw resume text
    raw_resume_text = request_data.get("rawResumeText", "")
    if not raw_resume_text:
        raw_resume_text = "No resume text provided."

    user_content = f"""You have already scored this candidate in an overview evaluation. Now provide DETAILED SUPPLEMENTARY analysis.
The scores should remain consistent with the overview. Focus on explanations, detail breakdowns, and suggestions.
If scoring rules were provided for criteria, reference them in your explanations.

=== EXISTING OVERVIEW SCORES ===
{overview_text}

=== JOB INFORMATION ===
Job Title: {request_data.get("jobTitle", "N/A")}

Scoring Criteria:
{criteria_text}

=== CANDIDATE RESUME (RAW TEXT) ===
Candidate Name: {request_data.get("candidateFullName", "N/A")}
Resume Name: {request_data.get("resumeName", "N/A")}

{raw_resume_text}

Based on the overview scores above, provide:
1. Detailed aiExplanation for EACH criterion that was scored
2. Exhaustive details list per criterion with status (MATCHED/MISSING/FIXED) and suggestions
3. isTrueLevel and hasRelatedExperience assessments
4. transferabilityToRole assessment

Keep scores consistent with the overview. Focus ONLY on explanations, details, and suggestions.
Return the complete supplement as JSON."""

    return [
        {
            "role": "system",
            "content": MATCHING_DETAIL_SUPPLEMENT_SYSTEM_PROMPT,
        },
        {
            "role": "user",
            "content": user_content,
        },
    ]
