package com.sma.core.service;

import com.sma.core.dto.request.resume.UpdateResumeProjectRequest;
import com.sma.core.dto.response.resume.ResumeProjectResponse;

public interface ResumeProjectService {
    ResumeProjectResponse create(Integer resumeId, UpdateResumeProjectRequest request);

    ResumeProjectResponse update(Integer resumeId, Integer projectId, UpdateResumeProjectRequest request);
}
