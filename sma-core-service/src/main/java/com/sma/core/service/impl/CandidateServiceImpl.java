package com.sma.core.service.impl;

import com.sma.core.dto.request.candidate.UpdateCandidateProfileRequest;
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
        Candidate candidate = getCurrentCandidate();

        return candidateMapper.toCandidateMyInfoResponse(candidate);
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateProfileResponse getMyProfile() {
        Candidate candidate = getCurrentCandidate();
        return buildCandidateProfile(candidate);
    }

    @Override
    @Transactional
    public CandidateProfileResponse updateMyProfile(UpdateCandidateProfileRequest request) {
        Candidate candidate = getCurrentCandidate();

        candidateProfileMapper.updateCandidateFromRequest(request, candidate);
        if (candidate.getUser() != null) {
            candidateProfileMapper.updateUserFromRequest(request, candidate.getUser());
        }

        candidateRepository.save(candidate);
        return buildCandidateProfile(candidate);
    }

    private CandidateProfileResponse buildCandidateProfile(Candidate candidate) {
        Integer candidateId = candidate.getId();

        Resume profileResume = resumeRepository
                .findFirstByCandidate_IdAndTypeOrderByIdDesc(candidateId, ResumeType.PROFILE)
                .orElse(null);

        ResumeDetailResponse profileResumeDetail = profileResume != null
                ? resumeDetailMapper.toDetailResponse(profileResume)
                : null;

        return candidateProfileMapper.mergeWithProfileResume(candidate, profileResumeDetail);
    }

    private Candidate getCurrentCandidate() {
        Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
        return candidateRepository.findById(candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.CANDIDATE_NOT_EXISTED));
    }
}
