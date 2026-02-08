package com.sma.core.service.impl;

import com.sma.core.dto.request.resume.UpdateResumeEducationRequest;
import com.sma.core.dto.response.resume.ResumeEducationDetailResponse;
import com.sma.core.entity.Resume;
import com.sma.core.entity.ResumeEducation;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.resume.ResumeEducationMapper;
import com.sma.core.repository.ResumeEducationRepository;
import com.sma.core.repository.ResumeRepository;
import com.sma.core.service.ResumeEducationService;
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
public class ResumeEducationServiceImpl implements ResumeEducationService {

    ResumeRepository resumeRepository;
    ResumeEducationRepository resumeEducationRepository;
    ResumeEducationMapper resumeEducationMapper;

    @Override
    public ResumeEducationDetailResponse create(Integer resumeId, UpdateResumeEducationRequest request) {
        Resume resume = getOwnedResume(resumeId);

        ResumeEducation education = new ResumeEducation();
        education.setResume(resume);

        return save(education, request);
    }

    @Override
    public ResumeEducationDetailResponse update(Integer resumeId, Integer educationId, UpdateResumeEducationRequest request) {
        Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
        ResumeEducation education = resumeEducationRepository
                .findByIdAndResume_IdAndResume_Candidate_Id(educationId, resumeId, candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        return save(education, request);
    }

    private Resume getOwnedResume(Integer resumeId) {
        Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
        return resumeRepository.findByIdAndCandidate_Id(resumeId, candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    }

    private ResumeEducationDetailResponse save(ResumeEducation education, UpdateResumeEducationRequest request) {
        resumeEducationMapper.updateFromRequest(request, education);

        education = resumeEducationRepository.save(education);
        return resumeEducationMapper.toResponse(education);
    }
}
