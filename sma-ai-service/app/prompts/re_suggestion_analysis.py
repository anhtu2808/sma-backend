"""
Prompt builder for re-suggesting career advice for a single weakness.
"""

from typing import Dict, Any

RE_SUGGESTION_SYSTEM_PROMPT = """You are a world-class senior career advisor and recruitment specialist.
Your task is to analyze a candidate's profile against a target job's requirements and provide constructive, highly structured, and actionable advice specifically to mitigate ONE single identified weakness.

You will receive:
1. Job Information (Title, Level)
2. Evaluation Summary & Matches
3. A specific "Weakness" (an area of concern in the profile)

Your responsibility is to generate ONE specific, actionable suggestion for this weakness.
If the input provides an "Existing Suggestion", your new suggestion should be a different or improved approach to solving the same weakness.

Return valid JSON only. No markdown, no explanation outside the JSON.

Rules:
1. Use exact camelCase keys as specified below.
2. The `suggestion` field must contain concise, actionable advice (2-4 sentences max) tailored specifically to the candidate's profile and the target job.
3. Your suggestion must be highly specific ("Take a certification in AWS Solutions Architect Associate") instead of generic ("Learn more cloud").
4. State how the candidate can mitigate the concern in an interview or address it through quick upskilling.

JSON structure:
{
  "weaknessSuggestion": [
    {
      "id": <integer ID from the input weakness>,
      "suggestion": "<actionable advice to mitigate this weakness>"
    }
  ],
  "gapSuggestion": []
}

IMPORTANT RULES:
- The output `weaknessSuggestion` array MUST contain exactly one entry for the weakness provided in the input, matching by `id`.
- The tone should be constructive, professional, and directly helpful to the candidate.
"""


def build_re_suggestion_prompt(request_data: Dict[str, Any]) -> list[dict]:
    """Build the OpenAI messages payload for re-suggestion generation.

    Args:
        request_data: The re-suggestion request data from Java, converted to dict.

    Returns:
        List of message dicts for OpenAI chat completion.
    """
    job_name = request_data.get("jobName", "Unknown Job")
    job_level = request_data.get("jobLevel", "Unknown Level")
    summary = request_data.get("summary", "No summary available")
    is_true_level = request_data.get("isTrueLevel")
    has_related_experience = request_data.get("hasRelatedExperience")
    match_level = request_data.get("matchLevel", "Unknown")

    level_eval = (
        f"Matches Required Level: {'Yes' if is_true_level else ('No' if is_true_level is False else 'Unknown')}"
    )
    experience_eval = (
        f"Has Related Experience: {'Yes' if has_related_experience else ('No' if has_related_experience is False else 'Unknown')}"
    )

    # Format the single weakness
    weaknesses_text = ""
    w = request_data.get("weakness")
    if w:
        weaknesses_text = (
            f"- ID: {w.get('id')}\n"
            f"  Weakness: {w.get('weaknessText', 'N/A')}\n"
            f"  Context: {w.get('context', 'N/A')}\n"
            f"  Criterion Type: {w.get('criterionType', 'N/A')}\n"
            f"  Severity (1-5): {w.get('severity', 'N/A')}\n"
            f"  Existing Suggestion (if any): {w.get('suggestion', 'None')}"
        )
    else:
        weaknesses_text = "No weakness provided for re-suggestion."

    user_content = f"""Please provide highly specific, actionable career advice for the following weakness to help the candidate qualify for this role.

=== JOB INFORMATION ===
Job Title: {job_name}
Job Level: {job_level}

=== EVALUATION SUMMARY ===
AI Summary: {summary}
Overall Match Level: {match_level}
{level_eval}
{experience_eval}

=== WEAKNESS REQUIRING RE-SUGGESTION ===
{weaknesses_text}

Generate ONE structured suggestion for the ID listed in the weakness above.
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
