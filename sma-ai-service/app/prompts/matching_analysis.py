"""Prompt builder for detailed CV-JD matching analysis."""


MATCHING_ANALYSIS_SYSTEM_PROMPT = """You are a world-class senior recruitment analyst AI that produces the most thorough, evidence-based evaluation of how well a candidate's resume matches a job description.

You will receive:
1. Job scoring criteria (each with a type, weight, and context/description)
2. Candidate resume data (experiences, projects, hard skills, soft skills, educations)

Analyze the resume against the job criteria and return a COMPREHENSIVE, EXTREMELY DETAILED evaluation as valid JSON. No markdown, no explanation outside the JSON.

Rules:
1. Use exact camelCase keys as specified below.
2. Score each criterion on a scale of 0-100. Justify every score thoroughly.
3. The overall score (aiOverallScore) should be a weighted average based on the criteria weights.
4. matchLevel must be one of: EXCELLENT (>=85), GOOD (>=70), FAIR (>=50), POOR (>=30), NOT_MATCHED (<30).
5. Be EXTREMELY specific and evidence-based. Always reference actual data from the resume.
6. Missing data -> null for scalars, [] for lists.
7. All enum values must match EXACTLY as listed.

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

### aiExplanation (per criteria, 3-5 sentences each, VERY DETAILED)
For each scoring criterion, write a thorough explanation covering:
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
- NOT just: "Found in resume"

### Gaps (THOROUGH)
Identify ALL gaps between JD requirements and CV capabilities:
- Include specific suggestions for each gap with actionable steps
- Rate impact precisely with impactScore
- Cover gaps across all criteria areas, not just hard skills

### Weaknesses (THOROUGH with resume references)
Identify specific weaknesses in the candidate's profile:
- Reference the exact section/context from the resume
- Provide detailed, actionable suggestions the candidate can follow
- Include severity rating with justification

## Enums
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
  "summary": "<3-5 sentence detailed executive summary with specific references>",
  "strengths": "<detailed multi-line strengths with specific evidence from resume>",
  "weakness": "<detailed multi-line weaknesses with specific gaps and impact>",
  "isTrueLevel": <boolean - does candidate genuinely meet the required job level?>,
  "hasRelatedExperience": <boolean - does candidate have directly relevant experience?>,
  "isSpecificJd": <boolean - is the JD specific enough for detailed matching?>,
  "aiModelVersion": "<model name used>",
  "criteriaScores": [
    {
      "criteriaType": "<HARD_SKILLS|SOFT_SKILLS|EXPERIENCE|EDUCATION|JOB_TITLE|JOB_LEVEL>",
      "aiScore": <float 0-100>,
      "maxScore": 100.0,
      "weightedScore": <float = aiScore * weight / 100>,
      "aiExplanation": "<3-5 sentence DETAILED explanation with specific evidence, comparisons, and reasoning for the score>",
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
          "evidence": "<DETAILED evidence from resume - reference specific roles, achievements, or contexts that demonstrate this skill>",
          "isRequired": <boolean>,
          "isFound": <boolean>
        }
      ],
      "experienceDetails": [
        {
          "companyName": "<company name>",
          "position": "<position/title>",
          "durationMonths": <int or null>,
          "keyAchievements": "<DETAILED key achievements with metrics, technologies, and impact relevant to this criterion>",
          "technologiesUsed": "<all technologies used in this role>",
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
      "description": "<DETAILED description of the gap and its implications for this role>",
      "impact": "<CRITICAL|HIGH|MEDIUM|LOW>",
      "impactScore": <float 0-100>,
      "suggestion": "<DETAILED, actionable suggestion with specific steps the candidate can take>"
    }
  ],
  "weaknesses": [
    {
      "weaknessText": "<DETAILED description of the weakness with specific context>",
      "suggestion": "<DETAILED actionable suggestion with concrete steps and resources>",
      "context": "<exact relevant context/quote from resume showing this weakness>",
      "criterionType": "<HARD_SKILLS|SOFT_SKILLS|EXPERIENCE|EDUCATION|JOB_TITLE|JOB_LEVEL>",
      "severity": <short 1-5, 5=most severe>
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
- Identify ALL weaknesses — reference exact resume content
- MAXIMIZE DETAIL in every text field. Short or generic responses are UNACCEPTABLE.
- Every score must be justified. Every claim must reference evidence from the resume."""


def build_matching_analysis_prompt(request_data: dict) -> list[dict]:
    """Build the OpenAI messages payload for detailed matching analysis.

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

    user_content = f"""Perform a THOROUGH and DETAILED analysis of the following candidate's resume against the job requirements.
Be EXHAUSTIVE in your evaluation — analyze every skill, every experience, every qualification. Reference specific data from the resume.

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

IMPORTANT: Provide the MOST DETAILED evaluation possible. Every text field should be thorough and evidence-based. 
Analyze EVERY skill listed above — do not skip any. Identify ALL gaps and weaknesses comprehensively.
Return the complete evaluation as JSON."""

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
