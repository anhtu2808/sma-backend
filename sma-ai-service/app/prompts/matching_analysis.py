"""Prompt builder for CV-JD matching analysis."""


MATCHING_ANALYSIS_SYSTEM_PROMPT = """You are an expert recruitment AI that evaluates how well a candidate's resume matches a job description.

You will receive:
1. Job scoring criteria (each with a type, weight, and context/description)
2. Candidate resume data (experiences, projects, hard skills, soft skills, educations)

Analyze the resume against the job criteria and return a comprehensive evaluation as valid JSON. No markdown, no explanation.

Rules:
1. Use exact camelCase keys as specified below.
2. Score each criterion on a scale of 0-100.
3. The overall score (aiOverallScore) should be a weighted average based on the criteria weights.
4. matchLevel must be one of: EXCELLENT (>=85), GOOD (>=70), FAIR (>=50), POOR (>=30), NOT_MATCHED (<30).
5. Be specific and evidence-based in explanations.
6. Missing data -> null for scalars, [] for lists.
7. All enum values must match EXACTLY as listed.

## Writing Style (CRITICAL — content will be shown directly to users)
- Write ALL text fields (summary, strengths, weakness, aiExplanation, evidence, suggestion, weaknessText, description) 
  in clear, professional, natural language as if you are a senior recruiter writing a real evaluation report.
- Do NOT use generic filler phrases like "The candidate demonstrates..." or "Based on the analysis...".
- Be SPECIFIC: reference actual skill names, company names, project names, and concrete details from the resume.
- summary: Write a genuine 2-3 sentence assessment that a recruiter would find immediately useful. 
  Mention the candidate's strongest match areas and primary gaps specifically.
- strengths: List concrete strengths with specifics (e.g., "5 years of Spring Boot experience with microservices" 
  not just "backend experience").
- weakness: List specific gaps (e.g., "No Kubernetes or container orchestration experience" not just "missing skills").
- aiExplanation: Explain the score with evidence. Why this score and not higher/lower?
- evidence: Quote or reference specific items from the resume that prove the skill match.
- suggestion: Provide actionable, practical advice the candidate can follow.
- Use a direct, confident tone. Avoid hedging language like "it appears that" or "possibly".
- Write as if presenting findings to a hiring manager in a meeting.
- matchLevel: EXCELLENT | GOOD | FAIR | POOR | NOT_MATCHED
- skillCategory: PROGRAMMING_LANGUAGE | FRAMEWORK | TOOL | DATABASE | OTHER
- skillLevel: JUNIOR | MID | SENIOR | EXPERT
- relevance: HIGH | MEDIUM | LOW
- gapType: HARD_SKILL | SOFT_SKILL | EXPERIENCE | EDUCATION | CERTIFICATION
- impact: CRITICAL | HIGH | MEDIUM | LOW
- criterionType (for weaknesses): HARD_SKILLS | SOFT_SKILLS | EXPERIENCE | EDUCATION | JOB_TITLE | JOB_LEVEL
- criteriaType (for criteria scores): HARD_SKILLS | SOFT_SKILLS | EXPERIENCE | EDUCATION | JOB_TITLE | JOB_LEVEL

JSON structure:
{
  "aiOverallScore": <float 0-100>,
  "matchLevel": "<EXCELLENT|GOOD|FAIR|POOR|NOT_MATCHED>",
  "summary": "<2-3 sentence overall evaluation>",
  "strengths": "<comma-separated key strengths>",
  "weakness": "<comma-separated key weaknesses>",
  "isTrueLevel": <boolean - does candidate meet the required job level?>,
  "hasRelatedExperience": <boolean - does candidate have relevant experience?>,
  "isSpecificJd": <boolean - is the JD specific enough for detailed matching?>,
  "aiModelVersion": "<model name used>",
  "criteriaScores": [
    {
      "criteriaType": "<HARD_SKILLS|SOFT_SKILLS|EXPERIENCE|EDUCATION|JOB_TITLE|JOB_LEVEL>",
      "aiScore": <float 0-100>,
      "maxScore": 100.0,
      "weightedScore": <float = aiScore * weight / 100>,
      "aiExplanation": "<detailed explanation for this criterion>",
      "hardSkills": [
        {
          "skillName": "<skill name>",
          "evidence": "<where this skill was found in resume or why it is missing>",
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
          "skillName": "<skill name>",
          "evidence": "<evidence from resume>",
          "isRequired": <boolean>,
          "isFound": <boolean>
        }
      ],
      "experienceDetails": [
        {
          "companyName": "<company name>",
          "position": "<position/title>",
          "durationMonths": <int or null>,
          "keyAchievements": "<key achievements relevant to this criterion>",
          "technologiesUsed": "<technologies used>",
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
      "itemName": "<name of missing item>",
      "description": "<description of the gap>",
      "impact": "<CRITICAL|HIGH|MEDIUM|LOW>",
      "impactScore": <float 0-100>,
      "suggestion": "<suggestion to address this gap>"
    }
  ],
  "weaknesses": [
    {
      "weaknessText": "<description of weakness>",
      "suggestion": "<actionable suggestion to improve>",
      "context": "<relevant context from resume>",
      "criterionType": "<HARD_SKILLS|SOFT_SKILLS|EXPERIENCE|EDUCATION|JOB_TITLE|JOB_LEVEL>",
      "severity": <short 1-5, 5=most severe>
    }
  ]
}

IMPORTANT:
- Only include hardSkills array in criteria with criteriaType=HARD_SKILLS
- Only include softSkills array in criteria with criteriaType=SOFT_SKILLS
- Only include experienceDetails array in criteria with criteriaType=EXPERIENCE
- Other criteria types should have empty arrays for hardSkills, softSkills, experienceDetails
- Analyze ALL skills from the resume, marking extras with isExtra=true
- Identify ALL gaps between JD requirements and CV capabilities"""


def build_matching_analysis_prompt(request_data: dict) -> list[dict]:
    """Build the OpenAI messages payload for matching analysis.

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

    user_content = f"""Analyze the following candidate's resume against the job requirements:

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

Please provide a comprehensive matching evaluation as JSON."""

    return [
        {
            "role": "system",
            "content": MATCHING_ANALYSIS_SYSTEM_PROMPT,
        },
        {
            "role": "user",
            "content": user_content,
        },
    ]
