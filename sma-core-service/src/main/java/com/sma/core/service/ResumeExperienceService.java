package com.sma.core.service;

import com.sma.core.dto.request.resume.UpdateResumeExperienceRequest;
import com.sma.core.dto.response.resume.ResumeExperienceResponse;

public interface ResumeExperienceService {
    ResumeExperienceResponse create(Integer resumeId, UpdateResumeExperienceRequest request);

    ResumeExperienceResponse update(Integer resumeId, Integer experienceId, UpdateResumeExperienceRequest request);
}
