package com.sma.core.service.impl;

import com.sma.core.dto.request.resume.UpdateResumeExperienceRequest;
import com.sma.core.dto.response.resume.ResumeExperienceResponse;
import com.sma.core.entity.Resume;
import com.sma.core.entity.ResumeExperience;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.resume.ResumeExperienceMapper;
import com.sma.core.repository.ResumeExperienceRepository;
import com.sma.core.repository.ResumeRepository;
import com.sma.core.service.ResumeExperienceService;
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
public class ResumeExperienceServiceImpl implements ResumeExperienceService {

    ResumeRepository resumeRepository;
    ResumeExperienceRepository resumeExperienceRepository;
    ResumeExperienceMapper resumeExperienceMapper;

    @Override
    public ResumeExperienceResponse create(Integer resumeId, UpdateResumeExperienceRequest request) {
        Resume resume = getOwnedResume(resumeId);

        ResumeExperience experience = new ResumeExperience();
        experience.setResume(resume);

        return save(experience, request);
    }

    @Override
    public ResumeExperienceResponse update(Integer resumeId, Integer experienceId, UpdateResumeExperienceRequest request) {
        Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
        ResumeExperience experience = resumeExperienceRepository
                .findByIdAndResume_IdAndResume_Candidate_Id(experienceId, resumeId, candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        return save(experience, request);
    }

    private Resume getOwnedResume(Integer resumeId) {
        Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
        return resumeRepository.findByIdAndCandidate_Id(resumeId, candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    }

    private ResumeExperienceResponse save(ResumeExperience experience, UpdateResumeExperienceRequest request) {
        resumeExperienceMapper.updateFromRequest(request, experience);
        if (experience.getOrderIndex() == null) {
            Integer maxOrderIndex = resumeExperienceRepository.findMaxOrderIndexByResumeId(experience.getResume().getId());
            experience.setOrderIndex(maxOrderIndex + 1);
        }

        experience = resumeExperienceRepository.save(experience);
        return resumeExperienceMapper.toResponse(experience);
    }
}
