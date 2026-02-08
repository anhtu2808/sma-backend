package com.sma.core.service.impl;

import com.sma.core.dto.request.resume.UpdateResumeExperienceDetailRequest;
import com.sma.core.dto.response.resume.ResumeExperienceDetailResponse;
import com.sma.core.entity.ResumeExperience;
import com.sma.core.entity.ResumeExperienceDetail;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.resume.ResumeExperienceDetailMapper;
import com.sma.core.repository.ResumeExperienceDetailRepository;
import com.sma.core.repository.ResumeExperienceRepository;
import com.sma.core.service.ResumeExperienceDetailService;
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
public class ResumeExperienceDetailServiceImpl implements ResumeExperienceDetailService {

    ResumeExperienceRepository resumeExperienceRepository;
    ResumeExperienceDetailRepository resumeExperienceDetailRepository;
    ResumeExperienceDetailMapper resumeExperienceDetailMapper;

    @Override
    public ResumeExperienceDetailResponse create(Integer resumeId, Integer experienceId, UpdateResumeExperienceDetailRequest request) {
        Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
        ResumeExperience experience = resumeExperienceRepository
                .findByIdAndResume_IdAndResume_Candidate_Id(experienceId, resumeId, candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        ResumeExperienceDetail detail = new ResumeExperienceDetail();
        detail.setExperience(experience);

        return save(detail, request);
    }

    @Override
    public ResumeExperienceDetailResponse update(Integer resumeId, Integer experienceDetailId, UpdateResumeExperienceDetailRequest request) {
        Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
        ResumeExperienceDetail detail = resumeExperienceDetailRepository
                .findByIdAndExperience_Resume_IdAndExperience_Resume_Candidate_Id(experienceDetailId, resumeId, candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        return save(detail, request);
    }

    private ResumeExperienceDetailResponse save(ResumeExperienceDetail detail, UpdateResumeExperienceDetailRequest request) {
        resumeExperienceDetailMapper.updateFromRequest(request, detail);

        detail = resumeExperienceDetailRepository.save(detail);
        return resumeExperienceDetailMapper.toResponse(detail);
    }
}
