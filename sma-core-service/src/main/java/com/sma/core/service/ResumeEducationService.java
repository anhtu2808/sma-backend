package com.sma.core.service;

import com.sma.core.dto.request.resume.UpdateResumeEducationRequest;
import com.sma.core.dto.response.resume.ResumeEducationDetailResponse;

public interface ResumeEducationService {
    ResumeEducationDetailResponse create(Integer resumeId, UpdateResumeEducationRequest request);

    ResumeEducationDetailResponse update(Integer resumeId, Integer educationId, UpdateResumeEducationRequest request);
}
