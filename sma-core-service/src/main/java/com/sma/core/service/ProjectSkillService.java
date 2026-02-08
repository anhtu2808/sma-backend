package com.sma.core.service;

import com.sma.core.dto.request.resume.UpdateProjectSkillRequest;
import com.sma.core.dto.response.resume.ProjectSkillResponse;

public interface ProjectSkillService {
    ProjectSkillResponse create(Integer resumeId, Integer projectId, UpdateProjectSkillRequest request);

    ProjectSkillResponse update(Integer resumeId, Integer projectSkillId, UpdateProjectSkillRequest request);
}
