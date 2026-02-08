package com.sma.core.service;

import com.sma.core.dto.request.resume.UpdateResumeExperienceDetailRequest;
import com.sma.core.dto.response.resume.ResumeExperienceDetailResponse;

public interface ResumeExperienceDetailService {
    ResumeExperienceDetailResponse create(Integer resumeId, Integer experienceId, UpdateResumeExperienceDetailRequest request);

    ResumeExperienceDetailResponse update(Integer resumeId, Integer experienceDetailId, UpdateResumeExperienceDetailRequest request);
}
