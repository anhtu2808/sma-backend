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
7. For context — these are the quoted texts from the raw resume that serve as evidence. Provide the exact string from the resume (or a short snippet).

CRITICAL CONSISTENCY AND MAPPING RULE:
- STRICTLY MAP each skill, experience, or qualification to the CORRECT criteria based on the criteria's Context.
- NEVER misclassify items (e.g., do not place Soft Skills under Hard Skills criteria, or Education items under Experience criteria).
- Avoid duplicating the same competency across multiple criteria.
- Each criterion must evaluate ONLY the aspect it is responsible for and must NOT overlap with other criteria.

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
- status: MATCHED (candidate has it and meets requirements), MISSING (required but candidate lacks it)
- description: DETAILED evidence — quote resume sections, reference specific projects/companies/durations
- requiredLevel / candidateLevel: For skills with level requirements (NONE | FRESHER | JUNIOR | MID | SENIOR | EXPERT) - ONLY use if criteria is related to technical or hard skills
- isRequired: Boolean indicating if this item is strictly required by the job
- context:
  - For MATCHED: exact quoted evidence from the raw resume (short snippet or short sentence).
  - For MISSING: REQUIRED. Return the exact resume line, section tail, or insertion position where the candidate can add the missing item. If there is no direct mention, point to the nearest valid place to insert it, such as the last visible line in the most relevant section.
  - For MISSING, context may be a full sentence or short paragraph, but it must be an exact unique substring or unique insertion anchor from the raw resume and must not repeat another identical context elsewhere in the raw text.
- impactScore: Only for MISSING items. It represents the estimated score improvement for THIS criterion (0-100) if the candidate fixes the missing item. MUST be null for MATCHED items.
- suggestions:
  - Only for MISSING items. MUST be short resume-ready lines that the candidate can directly paste into the CV.
  - Follow the same style as the re-suggestion prompt: concise, specific, and focused on rewriting or adding resume content rather than general learning advice.
  - Keep each suggestion short (ideally one resume bullet or one short line, max 1-2 sentences).
  - MATCHED items MUST have an empty suggestions list.

## Enums
- status (LabelStatus): MISSING | MATCHED
- skillLevel: NONE | FRESHER | JUNIOR | MID | SENIOR | EXPERT
- matchLevel: EXCELLENT | GOOD | FAIR | POOR | NOT_MATCHED
- transferabilityToRole: HIGH | MEDIUM | LOW

JSON structure:
{
  "matchLevel": "<EXCELLENT|GOOD|FAIR|POOR|NOT_MATCHED>",
  "summary": "<3-5 sentence detailed executive summary>",
  "strengths": "<detailed multi-line strengths>",
  "weakness": "<detailed multi-line weaknesses>",
  "isTrueLevel": <boolean - does candidate genuinely meet the required job level?>,
  "hasRelatedExperience": <boolean - does candidate have directly relevant experience?>,
  "transferabilityToRole": "<HIGH|MEDIUM|LOW>",
  "criteriaScores": [
    {
      "id": <int (exact ID of the criteria context provided)>,
      "aiScore": <float 0-100>,
      "aiExplanation": "<3-5 sentence DETAILED explanation with specific evidence, comparisons, and reasoning>",
      "details": [
        {
          "label": "<name of the item being evaluated>",
          "status": "<MATCHED|MISSING>",
          "description": "<DETAILED evidence — quote resume sections, reference specific projects/companies>",
          "requiredLevel": "<NONE|FRESHER|JUNIOR|MID|SENIOR|EXPERT or null> (only for HARD_SKILLS)",
          "candidateLevel": "<NONE|FRESHER|JUNIOR|MID|SENIOR|EXPERT or null> (only for HARD_SKILLS)",
          "isRequired": <boolean>,
          "context": "<exact evidence snippet for MATCHED, or REQUIRED exact insertion anchor from the resume for MISSING>",
          "impactScore": <float 0-100 represents the estimated score improvement for THIS criterion if the candidate fixes (only for MISSING)>,
          "suggestions": ["<short resume-ready line that can be pasted into the CV to improve matching>"]
        }
      ]
    }
  ]
}

IMPORTANT RULES:
- Analyze ALL relevant items for each criterion — be exhaustive
- Every MISSING item MUST have a non-empty context and at least one suggestion
- For MISSING items, context must identify exactly where the user can add the content in the resume, even if it is only an insertion point
- If a skill/keyword is missing entirely from the resume, do not leave context null; point to the closest valid place to add it
- Suggestions for MISSING items must be short CV lines, not generic career advice
- MATCHED items should have empty suggestions list and null impactScore
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
                f"ID: {c.get('id', 'N/A')}, "
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
                    f"ID: {cs.get('id', 'N/A')}, "
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
2. Exhaustive details list per criterion with status (MATCHED/MISSING) and suggestions
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
