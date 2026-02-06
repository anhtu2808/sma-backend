package com.sma.core.service.impl;

import com.sma.core.dto.response.myinfo.CandidateMyInfoResponse;
import com.sma.core.entity.Candidate;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.candidate.CandidateMapper;
import com.sma.core.repository.CandidateRepository;
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
    CandidateMapper candidateMapper;

    @Override
    @Transactional(readOnly = true)
    public CandidateMyInfoResponse getMyInfo() {
        Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.CANDIDATE_NOT_EXISTED));

        return candidateMapper.toCandidateMyInfoResponse(candidate);
    }
}
