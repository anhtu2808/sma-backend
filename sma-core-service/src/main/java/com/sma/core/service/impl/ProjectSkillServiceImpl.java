package com.sma.core.service.impl;

import com.sma.core.dto.request.resume.UpdateProjectSkillRequest;
import com.sma.core.dto.response.resume.ProjectSkillResponse;
import com.sma.core.entity.ProjectSkill;
import com.sma.core.entity.ResumeProject;
import com.sma.core.entity.Skill;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.resume.ProjectSkillMapper;
import com.sma.core.repository.ProjectSkillRepository;
import com.sma.core.repository.ResumeProjectRepository;
import com.sma.core.repository.SkillRepository;
import com.sma.core.service.ProjectSkillService;
import com.sma.core.utils.JwtTokenProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class ProjectSkillServiceImpl implements ProjectSkillService {

    ResumeProjectRepository resumeProjectRepository;
    ProjectSkillRepository projectSkillRepository;
    SkillRepository skillRepository;
    ProjectSkillMapper projectSkillMapper;

    @Override
    public ProjectSkillResponse create(Integer resumeId, Integer projectId, UpdateProjectSkillRequest request) {
        Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
        ResumeProject project = resumeProjectRepository
                .findByIdAndResume_IdAndResume_Candidate_Id(projectId, resumeId, candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        ProjectSkill projectSkill = new ProjectSkill();
        projectSkill.setProject(project);

        return save(projectSkill, request);
    }

    @Override
    public ProjectSkillResponse update(Integer resumeId, Integer projectSkillId, UpdateProjectSkillRequest request) {
        Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
        ProjectSkill projectSkill = projectSkillRepository
                .findByIdAndProject_Resume_IdAndProject_Resume_Candidate_Id(projectSkillId, resumeId, candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        return save(projectSkill, request);
    }

    private ProjectSkillResponse save(ProjectSkill projectSkill, UpdateProjectSkillRequest request) {
        projectSkillMapper.updateFromRequest(request, projectSkill);
        if (request.getSkillId() != null) {
            Skill skill = skillRepository.findById(request.getSkillId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
            projectSkill.setSkill(skill);
        }

        projectSkill = projectSkillRepository.save(projectSkill);
        return projectSkillMapper.toResponse(projectSkill);
    }
}
