package com.sma.core.service;

import com.sma.core.dto.request.resume.UpdateResumeSkillRequest;
import com.sma.core.dto.response.resume.ResumeSkillDetailResponse;

public interface ResumeSkillService {
    ResumeSkillDetailResponse create(Integer resumeId, UpdateResumeSkillRequest request);

    ResumeSkillDetailResponse update(Integer resumeId, Integer resumeSkillId, UpdateResumeSkillRequest request);

    void delete(Integer resumeId, Integer resumeSkillId);
}
