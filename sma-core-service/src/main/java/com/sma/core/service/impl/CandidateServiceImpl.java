package com.sma.core.service.impl;

import com.sma.core.dto.response.candidate.CandidateProfileResponse;
import com.sma.core.dto.response.resume.ResumeDetailResponse;
import com.sma.core.dto.response.myinfo.CandidateMyInfoResponse;
import com.sma.core.entity.Candidate;
import com.sma.core.entity.Resume;
import com.sma.core.enums.ResumeType;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.candidate.CandidateMapper;
import com.sma.core.mapper.candidate.CandidateProfileMapper;
import com.sma.core.mapper.resume.ResumeDetailMapper;
import com.sma.core.repository.CandidateRepository;
import com.sma.core.repository.ResumeRepository;
import com.sma.core.service.CandidateService;
import com.sma.core.utils.JwtTokenProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CandidateServiceImpl implements CandidateService {

    CandidateRepository candidateRepository;
    ResumeRepository resumeRepository;
    CandidateMapper candidateMapper;
    CandidateProfileMapper candidateProfileMapper;
    ResumeDetailMapper resumeDetailMapper;

    @Override
    @Transactional(readOnly = true)
    public CandidateMyInfoResponse getMyInfo() {
        Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.CANDIDATE_NOT_EXISTED));

        return candidateMapper.toCandidateMyInfoResponse(candidate);
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateProfileResponse getMyProfile() {
        Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.CANDIDATE_NOT_EXISTED));

        Resume profileResume = resumeRepository
                .findFirstByCandidate_IdAndTypeOrderByIdDesc(candidateId, ResumeType.PROFILE)
                .orElse(null);

        ResumeDetailResponse profileResumeDetail = profileResume != null
                ? resumeDetailMapper.toDetailResponse(profileResume)
                : null;

        return candidateProfileMapper.mergeWithProfileResume(candidate, profileResumeDetail);
    }
}
