package com.sma.core.service;

import com.sma.core.dto.request.resume.ExperienceSkillRequest;
import com.sma.core.dto.response.resume.ExperienceSkillResponse;

public interface ExperienceSkillService {
    ExperienceSkillResponse create(Integer resumeId, Integer experienceDetailId, ExperienceSkillRequest request);

    ExperienceSkillResponse update(Integer resumeId, Integer experienceSkillId, ExperienceSkillRequest request);
}
