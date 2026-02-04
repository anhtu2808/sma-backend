package com.sma.core.service.impl;

import com.sma.core.dto.request.resume.UploadResumeRequest;
import com.sma.core.dto.response.resume.ResumeResponse;
import com.sma.core.entity.Candidate;
import com.sma.core.entity.Resume;
import com.sma.core.entity.User;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.resume.ResumeMapper;
import com.sma.core.repository.CandidateRepository;
import com.sma.core.repository.ResumeRepository;
import com.sma.core.service.ResumeService;
import com.sma.core.utils.JwtTokenProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
public class ResumeServiceImpl implements ResumeService {
    final ResumeRepository resumeRepository;
    final CandidateRepository candidateRepository;

    final ResumeMapper resumeMapper;

    @Override
    public ResumeResponse uploadResume(UploadResumeRequest request) {
        Resume resume = resumeMapper.toEntity(request);
        Candidate candidate = candidateRepository.findById(JwtTokenProvider.getCurrentCandidateId())
                .orElseThrow(() -> new AppException(ErrorCode.CANDIDATE_NOT_EXISTED));
        resume.setCandidate(candidate);
        resume = resumeRepository.save(resume);
        return resumeMapper.toResponse(resume);
    }
}
