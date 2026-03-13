package com.sma.core.service.impl;

import com.sma.core.dto.request.company.BlockCandidateRequest;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.company.BlockedCandidateResponse;
import com.sma.core.entity.CompanyBlockedCandidate;
import com.sma.core.entity.Recruiter;
import com.sma.core.enums.ApplicationStatus;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.repository.*;
import com.sma.core.service.CompanyBlockedCandidateService;
import com.sma.core.utils.JwtTokenProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Transactional
public class CompanyBlockedCandidateServiceImpl implements CompanyBlockedCandidateService {
    CompanyBlockedCandidateRepository repository;
    RecruiterRepository recruiterRepository;
    CandidateRepository candidateRepository;
    UserRepository userRepository;
    ApplicationRepository applicationRepository;

    @Override
    public void blockCandidate(BlockCandidateRequest request) {
        Integer currentUserId = JwtTokenProvider.getCurrentUserId();

        Recruiter recruiter = recruiterRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED));
        Integer companyId = recruiter.getCompany().getId();

        if (repository.existsByCompanyIdAndCandidateIdAndIsDeletedFalse(companyId, request.getCandidateId())) {
            throw new AppException(ErrorCode.CANDIDATE_ALREADY_BLOCKED);
        }

        CompanyBlockedCandidate entry = CompanyBlockedCandidate.builder()
                .company(recruiter.getCompany())
                .candidate(candidateRepository.getReferenceById(request.getCandidateId()))
                .reason(request.getReason())
                .createdBy(userRepository.getReferenceById(currentUserId))
                .blockDate(LocalDateTime.now())
                .isDeleted(false)
                .build();

        repository.save(entry);
        List<ApplicationStatus> closedStatuses = List.of(
                ApplicationStatus.REJECTED,
                ApplicationStatus.APPROVED
        );

        applicationRepository.rejectAllApplicationsByBlock(
                request.getCandidateId(),
                companyId,
                ApplicationStatus.REJECTED,
                "Auto-rejected due to candidate being blocked by company",
                closedStatuses
        );

        log.info("Auto-rejected all pending applications for candidate {} after being blocked by company {}",
                request.getCandidateId(), companyId);
    }

    @Override
    public void unblockCandidate(Integer candidateId) {
        Integer currentUserId = JwtTokenProvider.getCurrentUserId();
        Recruiter recruiter = recruiterRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED));

        CompanyBlockedCandidate entry = repository
                .findByCompanyIdAndCandidateIdAndIsDeletedFalse(recruiter.getCompany().getId(), candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        entry.setIsDeleted(true);
        repository.save(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<BlockedCandidateResponse> getBlacklist(Pageable pageable, String keyword) {
        Integer currentUserId = JwtTokenProvider.getCurrentUserId();
        Recruiter recruiter = recruiterRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED));

        Page<CompanyBlockedCandidate> page = repository.findAllActiveByCompany(
                recruiter.getCompany().getId(), keyword, pageable);

        List<BlockedCandidateResponse> dtoList = page.getContent().stream()
                .map(item -> BlockedCandidateResponse.builder()
                        .candidateId(item.getCandidate().getId())
                        .fullName(item.getCandidate().getUser().getFullName())
                        .email(item.getCandidate().getUser().getEmail())
                        .reason(item.getReason())
                        .blockDate(item.getBlockDate())
                        .createdBy(item.getCreatedBy().getEmail())
                        .blockedById(item.getCreatedBy().getId())
                        .build())
                .toList();

        return PagingResponse.fromPage(page, dtoList);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCandidateBlocked(Integer candidateId, Integer companyId) {
        return repository.existsByCompanyIdAndCandidateIdAndIsDeletedFalse(companyId, candidateId);
    }
}
