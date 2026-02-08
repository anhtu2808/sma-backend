package com.sma.core.service.impl;

import com.sma.core.dto.request.resume.UpdateResumeCertificationRequest;
import com.sma.core.dto.response.resume.ResumeCertificationDetailResponse;
import com.sma.core.entity.Resume;
import com.sma.core.entity.ResumeCertification;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.resume.ResumeCertificationMapper;
import com.sma.core.repository.ResumeCertificationRepository;
import com.sma.core.repository.ResumeRepository;
import com.sma.core.service.ResumeCertificationService;
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
public class ResumeCertificationServiceImpl implements ResumeCertificationService {

    ResumeRepository resumeRepository;
    ResumeCertificationRepository resumeCertificationRepository;
    ResumeCertificationMapper resumeCertificationMapper;

    @Override
    public ResumeCertificationDetailResponse create(Integer resumeId, UpdateResumeCertificationRequest request) {
        Resume resume = getOwnedResume(resumeId);

        ResumeCertification certification = new ResumeCertification();
        certification.setResume(resume);

        return save(certification, request);
    }

    @Override
    public ResumeCertificationDetailResponse update(Integer resumeId, Integer certificationId, UpdateResumeCertificationRequest request) {
        Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
        ResumeCertification certification = resumeCertificationRepository
                .findByIdAndResume_IdAndResume_Candidate_Id(certificationId, resumeId, candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        return save(certification, request);
    }

    private Resume getOwnedResume(Integer resumeId) {
        Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
        return resumeRepository.findByIdAndCandidate_Id(resumeId, candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    }

    private ResumeCertificationDetailResponse save(ResumeCertification certification, UpdateResumeCertificationRequest request) {
        resumeCertificationMapper.updateFromRequest(request, certification);

        certification = resumeCertificationRepository.save(certification);
        return resumeCertificationMapper.toResponse(certification);
    }
}
