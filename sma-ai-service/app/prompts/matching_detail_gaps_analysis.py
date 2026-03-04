"""Prompt builder for detail supplement analysis — adds explanations, nested skills, gaps & weaknesses to existing overview scores."""


MATCHING_DETAIL_SUPPLEMENT_SYSTEM_PROMPT = """You are a world-class senior recruitment analyst AI that provides DEEP, DETAILED analysis supplements for CV-JD matching.

You have already received an overview scoring (criteria scores, summary, strengths, weakness). Your task is to SUPPLEMENT that overview with:
1. Detailed per-criteria explanations (aiExplanation) with specific evidence
2. Nested skill breakdowns (hardSkills, softSkills, experienceDetails) for each criterion
3. Gap analysis between JD requirements and CV capabilities
4. Detailed weakness analysis

Return valid JSON only. No markdown, no explanation outside the JSON.

Rules:
1. Use exact camelCase keys as specified below.
2. Do NOT re-score criteria — the scores are already determined from the overview.
3. Be EXTREMELY specific and evidence-based. Always reference actual data from the resume.
4. Missing data -> null for scalars, [] for lists.
5. All enum values must match EXACTLY as listed.

## Writing Style (CRITICAL — content will be shown directly to hiring managers and recruiters)

### aiExplanation (per criteria, 3-5 sentences each, VERY DETAILED)
For each scoring criterion that exists in the overview, write a thorough explanation covering:
- What specific evidence supports the score
- What is missing that prevented a higher score
- How the candidate's experience level compares to requirements
- Any notable strengths or concerns within this criterion
- Reference specific skills, companies, projects, durations by name

### evidence (per skill, DETAILED)
For each hard skill and soft skill:
- Quote or reference the EXACT place in the resume where the skill appears
- If missing, explain what was expected and what was found instead
- Include context: "Used Spring Boot at Company X for 3 years building REST APIs with 99.9% uptime"

### Gaps (THOROUGH)
Identify ALL gaps between JD requirements and CV capabilities:
- Rate impact precisely with impactScore
- Cover gaps across all criteria areas, not just hard skills

### Weaknesses (THOROUGH with resume references)
Identify specific weaknesses in the candidate's profile:
- Reference the exact section/context from the resume
- Include severity rating with justification

## Enums
- skillCategory: PROGRAMMING_LANGUAGE | FRAMEWORK | TOOL | DATABASE | OTHER
- skillLevel: JUNIOR | MID | SENIOR | EXPERT
- relevance: HIGH | MEDIUM | LOW
- gapType: HARD_SKILL | SOFT_SKILL | EXPERIENCE | EDUCATION | CERTIFICATION
- impact: CRITICAL | HIGH | MEDIUM | LOW
- criterionType (for weaknesses): HARD_SKILLS | SOFT_SKILLS | EXPERIENCE | EDUCATION | JOB_TITLE | JOB_LEVEL
- criteriaType (for criteria details): HARD_SKILLS | SOFT_SKILLS | EXPERIENCE | EDUCATION | JOB_TITLE | JOB_LEVEL

JSON structure:
{
  "isTrueLevel": <boolean - does candidate genuinely meet the required job level?>,
  "hasRelatedExperience": <boolean - does candidate have directly relevant experience?>,
  "criteriaScores": [
    {
      "criteriaType": "<HARD_SKILLS|SOFT_SKILLS|EXPERIENCE|EDUCATION|JOB_TITLE|JOB_LEVEL>",
      "aiExplanation": "<3-5 sentence DETAILED explanation with specific evidence, comparisons, and reasoning>",
      "hardSkills": [
        {
          "skillName": "<exact skill name>",
          "evidence": "<DETAILED evidence - quote resume sections, reference specific projects/companies/durations>",
          "skillCategory": "<PROGRAMMING_LANGUAGE|FRAMEWORK|TOOL|DATABASE|OTHER>",
          "requiredLevel": "<JUNIOR|MID|SENIOR|EXPERT or null>",
          "candidateLevel": "<JUNIOR|MID|SENIOR|EXPERT or null>",
          "matchScore": <float 0-100>,
          "yearsOfExperience": <float or null>,
          "isCritical": <boolean>,
          "isMatched": <boolean>,
          "isMissing": <boolean>,
          "isExtra": <boolean>,
          "relevance": "<HIGH|MEDIUM|LOW>"
        }
      ],
      "softSkills": [
        {
          "skillName": "<exact skill name>",
          "evidence": "<DETAILED evidence from resume>",
          "isRequired": <boolean>,
          "isFound": <boolean>
        }
      ],
      "experienceDetails": [
        {
          "companyName": "<company name>",
          "position": "<position/title>",
          "durationMonths": <int or null>,
          "keyAchievements": "<DETAILED key achievements>",
          "technologiesUsed": "<all technologies used>",
          "isRelevant": <boolean>,
          "transferabilityToRole": "<HIGH|MEDIUM|LOW>",
          "experienceGravity": "<HIGH|MEDIUM|LOW>"
        }
      ]
    }
  ],
  "gaps": [
    {
      "gapType": "<HARD_SKILL|SOFT_SKILL|EXPERIENCE|EDUCATION|CERTIFICATION>",
      "itemName": "<specific name of missing item>",
      "description": "<DETAILED description of the gap>",
      "impact": "<CRITICAL|HIGH|MEDIUM|LOW>",
      "impactScore": <float 0-100>
    }
  ],
  "weaknesses": [
    {
      "weaknessText": "<DETAILED description of the weakness>",
      "context": "<exact relevant context from resume>",
      "criterionType": "<HARD_SKILLS|SOFT_SKILLS|EXPERIENCE|EDUCATION|JOB_TITLE|JOB_LEVEL>",
      "severity": <int 1-5, 5=most severe>
    }
  ]
}

IMPORTANT RULES:
- Only include hardSkills array in criteria with criteriaType=HARD_SKILLS
- Only include softSkills array in criteria with criteriaType=SOFT_SKILLS
- Only include experienceDetails array in criteria with criteriaType=EXPERIENCE
- Other criteria types should have empty arrays for hardSkills, softSkills, experienceDetails
- Analyze ALL skills from the resume — mark extras with isExtra=true
- Identify ALL gaps between JD requirements and CV capabilities — be exhaustive
- MAXIMIZE DETAIL in every text field.
- Every claim must reference evidence from the resume."""


def build_matching_detail_supplement_prompt(request_data: dict) -> list[dict]:
    """Build the OpenAI messages payload for detail supplement analysis.

    This prompt includes the overview scores as context so the AI only needs to
    supplement with explanations, skill breakdowns, gaps, and weaknesses.

    Args:
        request_data: The matching request data containing job criteria, resume data,
                      AND overview scores from the existing evaluation.

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
                    f"score={cs.get('aiScore', 'N/A')}, "
                    f"weighted={cs.get('weightedScore', 'N/A')}"
                )
        overview_text = "\n".join(overview_lines)
    else:
        overview_text = "No overview scores available."

    # Format experiences
    experiences_text = ""
    experiences = request_data.get("experiences", [])
    if experiences:
        exp_lines = []
        for exp in experiences:
            exp_lines.append(f"\n  Company: {exp.get('company', 'N/A')}")
            exp_lines.append(f"  Working Model: {exp.get('workingModel', 'N/A')}")
            exp_lines.append(f"  Employment Type: {exp.get('employmentType', 'N/A')}")
            for detail in exp.get("details", []):
                exp_lines.append(f"    - Title: {detail.get('title', 'N/A')}")
                exp_lines.append(f"      Description: {detail.get('description', 'N/A')}")
                skills = detail.get("skills", [])
                if skills:
                    skill_descs = [s.get("description", "") for s in skills if s.get("description")]
                    if skill_descs:
                        exp_lines.append(f"      Skills: {', '.join(skill_descs)}")
        experiences_text = "\n".join(exp_lines)
    else:
        experiences_text = "No experience data available."

    # Format projects
    projects_text = ""
    projects = request_data.get("projects", [])
    if projects:
        proj_lines = []
        for proj in projects:
            proj_lines.append(f"\n  Title: {proj.get('title', 'N/A')}")
            proj_lines.append(f"  Team Size: {proj.get('teamSize', 'N/A')}")
            proj_lines.append(f"  Type: {proj.get('projectType', 'N/A')}")
            proj_lines.append(f"  Description: {proj.get('description', 'N/A')}")
            skills = proj.get("skills", [])
            if skills:
                skill_descs = [s.get("description", "") for s in skills if s.get("description")]
                if skill_descs:
                    proj_lines.append(f"  Skills: {', '.join(skill_descs)}")
        projects_text = "\n".join(proj_lines)
    else:
        projects_text = "No project data available."

    # Format hard skills
    hard_skills_text = ""
    hard_skills = request_data.get("hardSkills", [])
    if hard_skills:
        hs_lines = []
        for hs in hard_skills:
            yoe = hs.get("yearsOfExperience")
            yoe_str = f" ({yoe} years)" if yoe else ""
            hs_lines.append(f"- {hs.get('name', 'N/A')}{yoe_str}")
        hard_skills_text = "\n".join(hs_lines)
    else:
        hard_skills_text = "No hard skills listed."

    # Format soft skills
    soft_skills_text = ""
    soft_skills = request_data.get("softSkills", [])
    if soft_skills:
        ss_lines = [f"- {ss.get('name', 'N/A')}" for ss in soft_skills]
        soft_skills_text = "\n".join(ss_lines)
    else:
        soft_skills_text = "No soft skills listed."

    # Format educations
    educations_text = ""
    educations = request_data.get("educations", [])
    if educations:
        edu_lines = []
        for edu in educations:
            edu_lines.append(
                f"- {edu.get('degree', 'N/A')} in {edu.get('majorField', 'N/A')} "
                f"at {edu.get('institution', 'N/A')}"
                + (f" (GPA: {edu.get('gpa')})" if edu.get("gpa") else "")
            )
        educations_text = "\n".join(edu_lines)
    else:
        educations_text = "No education data available."

    user_content = f"""You have already scored this candidate in an overview evaluation. Now provide DETAILED SUPPLEMENTARY analysis.

=== EXISTING OVERVIEW SCORES ===
{overview_text}

=== JOB INFORMATION ===
Job Title: {request_data.get("jobTitle", "N/A")}

Scoring Criteria:
{criteria_text}

=== CANDIDATE INFORMATION ===
Candidate Name: {request_data.get("candidateFullName", "N/A")}
Resume Name: {request_data.get("resumeName", "N/A")}

Hard Skills:
{hard_skills_text}

Soft Skills:
{soft_skills_text}

Work Experience:
{experiences_text}

Projects:
{projects_text}

Education:
{educations_text}

Based on the overview scores above, provide:
1. Detailed aiExplanation for EACH criterion that was scored
2. Nested hardSkills/softSkills/experienceDetails breakdowns per criterion
3. Comprehensive gaps analysis
4. Detailed weaknesses analysis
5. isTrueLevel and hasRelatedExperience assessments

Do NOT re-score — use the existing scores as context. Focus ONLY on explanations, skill details, gaps, and weaknesses.
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
