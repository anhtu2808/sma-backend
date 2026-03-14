"""
Prompt builder for re-suggesting career advice for a single weakness.
"""

from typing import Any, Dict

RE_SUGGESTION_SYSTEM_PROMPT = """You are a world-class senior career advisor and recruitment specialist.
Your task is to analyze a candidate's resume against a target job's requirements and provide one improved suggestion that tells the candidate how to revise the resume so it better fits the job.

Return valid JSON only. No markdown, no explanation outside the JSON.

Rules:
1. Use exact camelCase keys as specified below.
2. The `suggestion` field must contain concise, actionable advice (max 1–2 sentences) tailored specifically to the candidate's profile and the target job.
3. The suggestion must tell the candidate what to change, add, rewrite, clarify, or emphasize in the resume.
4. Prefer a rewritten resume bullet that can be copied directly into the resume.
5. Focus on resume edits, not general learning advice, interview advice, or long-term career advice, unless it directly supports a concrete resume rewrite.
6. `weakness` is the overall weakness category and should be read alongside the summary.
7. `description` is the specific error/problem that caused the weakness.
8. `context` is the exact content or area in the resume that needs to be revised.
9. `aiExplanation` explains the overall scoring logic for that scoring criterion and should be used to understand why the resume was judged that way.
10. If `rule` is present, use both `rule` and `scoringCriteriaContext`. If `rule` is missing or null, rely on `scoringCriteriaContext` only.
11. If an existing suggestion is provided, create a different or improved approach.
12. Always include the same `suggestionId` from the input.

JSON structure:
{
  "suggestionId": <integer or null>,
  "suggestion": "<actionable advice to mitigate this weakness>"
}
"""


def build_re_suggestion_prompt(request_data: Dict[str, Any]) -> list[dict]:
    """Build the OpenAI messages payload for re-suggestion generation."""
    suggestion_id = request_data.get("suggestionId")
    job_title = request_data.get("jobTitle", "Unknown Job")
    job_level = request_data.get("jobLevel", "Unknown Level")
    summary = request_data.get("summary", "No summary available")
    weakness = request_data.get("weakness", "No weakness provided")
    scoring_criteria_context = request_data.get("scoringCriteriaContext", "N/A")
    rule = request_data.get("rule", "N/A")
    label = request_data.get("label", "N/A")
    context = request_data.get("context", "N/A")
    suggestion = request_data.get("suggestion", "None")
    ai_explanation = request_data.get("aiExplanation", "N/A")
    description = request_data.get("description", "N/A")
    has_rule = rule not in (None, "", "N/A")

    scoring_guidance = (
        f"Rule: {rule}\nScoring Criteria Context: {scoring_criteria_context}"
        if has_rule
        else f"Scoring Criteria Context: {scoring_criteria_context}"
    )

    user_content = f"""Please provide one improved, highly specific suggestion to help the candidate revise the resume so it better matches this role.

=== JOB INFORMATION ===
Suggestion ID: {suggestion_id}
Job Title: {job_title}
Job Level: {job_level}

=== EVALUATION SUMMARY ===
Summary: {summary}

=== WEAKNESS DETAIL ===
Overall Weakness: {weakness}
Label: {label}
Specific Error Description: {description}
Resume Content To Revise: {context}
Scoring Guidance:
{scoring_guidance}
AI Explanation Of Overall Scoring: {ai_explanation}
Existing Suggestion: {suggestion}

The new suggestion must clearly explain how to rewrite or improve the resume content in the area described by "Resume Content To Revise" so the profile aligns better with the job and scoring expectations.

Return the result as JSON matching the specified schema.
"""

    return [
        {
            "role": "system",
            "content": RE_SUGGESTION_SYSTEM_PROMPT,
        },
        {
            "role": "user",
            "content": user_content,
        },
    ]
