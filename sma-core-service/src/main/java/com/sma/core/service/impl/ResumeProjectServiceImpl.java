package com.sma.core.service.impl;

import com.sma.core.dto.request.resume.UpdateResumeProjectRequest;
import com.sma.core.dto.response.resume.ResumeProjectResponse;
import com.sma.core.entity.Resume;
import com.sma.core.entity.ResumeProject;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.resume.ResumeProjectMapper;
import com.sma.core.repository.ResumeProjectRepository;
import com.sma.core.repository.ResumeRepository;
import com.sma.core.service.ResumeProjectService;
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
public class ResumeProjectServiceImpl implements ResumeProjectService {

    ResumeRepository resumeRepository;
    ResumeProjectRepository resumeProjectRepository;
    ResumeProjectMapper resumeProjectMapper;

    @Override
    public ResumeProjectResponse create(Integer resumeId, UpdateResumeProjectRequest request) {
        Resume resume = getOwnedResume(resumeId);

        ResumeProject project = new ResumeProject();
        project.setResume(resume);

        return save(project, request);
    }

    @Override
    public ResumeProjectResponse update(Integer resumeId, Integer projectId, UpdateResumeProjectRequest request) {
        Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
        ResumeProject project = resumeProjectRepository
                .findByIdAndResume_IdAndResume_Candidate_Id(projectId, resumeId, candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        return save(project, request);
    }

    private Resume getOwnedResume(Integer resumeId) {
        Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
        return resumeRepository.findByIdAndCandidate_Id(resumeId, candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    }

    private ResumeProjectResponse save(ResumeProject project, UpdateResumeProjectRequest request) {
        resumeProjectMapper.updateFromRequest(request, project);
        if (project.getOrderIndex() == null) {
            Integer maxOrderIndex = resumeProjectRepository.findMaxOrderIndexByResumeId(project.getResume().getId());
            project.setOrderIndex(maxOrderIndex + 1);
        }

        project = resumeProjectRepository.save(project);
        return resumeProjectMapper.toResponse(project);
    }
}
