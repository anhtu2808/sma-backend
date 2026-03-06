"""
Prompt builder for suggestion generation — creates career advice to resolve gaps and weaknesses.
"""

from typing import Dict, Any

SUGGESTION_SYSTEM_PROMPT = """You are a world-class senior career advisor and recruitment specialist.
Your task is to analyze a candidate's profile against a target job's requirements and provide constructive, highly structured, and actionable suggestions to improve their chances of success.

You will receive:
1. Job Information (Title, Level)
2. Evaluation Summary & Matches
3. A list of specific "Gaps" (missing skills, experience, etc.)
4. A list of specific "Weaknesses" (areas of concern in the profile)

Your responsibility is to generate ONE specific, actionable suggestion for EVERY gap and EVERY weakness provided in the input.

Return valid JSON only. No markdown, no explanation outside the JSON.

Rules:
1. Use exact camelCase keys as specified below.
2. The `suggestion` field must contain concise, actionable advice (2-4 sentences max) tailored specifically to the candidate's profile and the target job.
3. Your suggestions must be highly specific ("Take a certification in AWS Solutions Architect Associate") instead of generic ("Learn more cloud").
4. For weaknesses, state how the candidate can mitigate the concern in an interview or address it through quick upskilling.
5. For gaps, state exactly what they need to learn, do, or demonstrate to close the gap.

JSON structure:
{
  "gapSuggestion": [
    {
      "id": <integer ID from the input gap>,
      "suggestion": "<actionable advice to resolve this gap>"
    }
  ],
  "weaknessSuggestion": [
    {
      "id": <integer ID from the input weakness>,
      "suggestion": "<actionable advice to mitigate this weakness>"
    }
  ]
}

IMPORTANT RULES:
- The output `gapSuggestion` array MUST contain exactly one entry for EVERY gap provided in the input, matching by `id`.
- The output `weaknessSuggestion` array MUST contain exactly one entry for EVERY weakness provided in the input, matching by `id`.
- Missing an ID from the input will cause system failure. Ensure all input IDs are mapped to a suggestion.
- The tone should be constructive, professional, and directly helpful to the candidate.
"""


def build_suggestion_prompt(request_data: Dict[str, Any]) -> list[dict]:
    """Build the OpenAI messages payload for suggestion generation.

    Args:
        request_data: The suggestion request data from Java, converted to dict.

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

    # Format gaps
    gaps_text = ""
    gaps = request_data.get("gaps", [])
    if gaps:
        gap_lines = []
        for g in gaps:
            gap_lines.append(
                f"- ID: {g.get('id')}\n"
                f"  Type: {g.get('gapType', 'N/A')}\n"
                f"  Item: {g.get('itemName', 'N/A')}\n"
                f"  Description: {g.get('description', 'N/A')}\n"
                f"  Impact: {g.get('impact', 'N/A')}"
            )
        gaps_text = "\n\n".join(gap_lines)
    else:
        gaps_text = "No gaps identified."

    # Format weaknesses
    weaknesses_text = ""
    weaknesses = request_data.get("weaknesses", [])
    if weaknesses:
        weakness_lines = []
        for w in weaknesses:
            weakness_lines.append(
                f"- ID: {w.get('id')}\n"
                f"  Weakness: {w.get('weaknessText', 'N/A')}\n"
                f"  Context: {w.get('context', 'N/A')}\n"
                f"  Criterion Type: {w.get('criterionType', 'N/A')}\n"
                f"  Severity (1-5): {w.get('severity', 'N/A')}\n"
                f"  Existing Suggestion (if any): {w.get('suggestion', 'None')}"
            )
        weaknesses_text = "\n\n".join(weakness_lines)
    else:
        weaknesses_text = "No weaknesses identified."

    user_content = f"""Please provide highly specific, actionable career advice for the following gaps and weaknesses to help the candidate qualify for this role.

=== JOB INFORMATION ===
Job Title: {job_name}
Job Level: {job_level}

=== EVALUATION SUMMARY ===
AI Summary: {summary}
Overall Match Level: {match_level}
{level_eval}
{experience_eval}

=== GAPS REQUIRING SUGGESTIONS ===
{gaps_text}

=== WEAKNESSES REQUIRING SUGGESTIONS ===
{weaknesses_text}

Generate ONE structured suggestion for EVERY ID listed in the gaps and weaknesses above.
Return the result as JSON matching the specified schema.
"""

    return [
        {
            "role": "system",
            "content": SUGGESTION_SYSTEM_PROMPT,
        },
        {
            "role": "user",
            "content": user_content,
        },
    ]
