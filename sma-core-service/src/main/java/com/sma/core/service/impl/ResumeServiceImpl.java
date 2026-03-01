package com.sma.core.service.impl;

import com.sma.core.dto.request.resume.UpdateResumeRequest;
import com.sma.core.dto.request.resume.UploadResumeRequest;
import com.sma.core.dto.response.resume.ResumeDetailResponse;
import com.sma.core.dto.response.resume.ResumeResponse;
import com.sma.core.entity.Candidate;
import com.sma.core.enums.FeatureKey;
import com.sma.core.entity.Resume;
import com.sma.core.enums.ResumeParseStatus;
import com.sma.core.enums.UsageEntityType;
import com.sma.core.enums.ResumeStatus;
import com.sma.core.enums.ResumeType;
import com.sma.core.enums.Role;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.resume.ResumeDetailMapper;
import com.sma.core.mapper.resume.ResumeMapper;
import com.sma.core.messaging.resume.ResumeParsingRequestPublisher;
import com.sma.core.repository.ApplicationRepository;
import com.sma.core.repository.CandidateRepository;
import com.sma.core.repository.ResumeRepository;
import com.sma.core.service.ResumeService;
import com.sma.core.service.ResumeCloneService;
import com.sma.core.service.QuotaService;
import com.sma.core.specification.ResumeSpecification;
import com.sma.core.utils.JwtTokenProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
public class ResumeServiceImpl implements ResumeService {
    final ResumeRepository resumeRepository;
    final ApplicationRepository applicationRepository;
    final CandidateRepository candidateRepository;
    final ResumeMapper resumeMapper;
    final ResumeDetailMapper resumeDetailMapper;
    final ResumeParsingRequestPublisher resumeParsingRequestPublisher;
    final ResumeCloneService resumeCloneService;
    final QuotaService quotaService;

    @Override
    @Transactional(readOnly = true)
    public List<ResumeResponse> getMyResumes(String keyword, ResumeType type) {
        Candidate candidate = getCurrentCandidate();
        List<Resume> resumes = resumeRepository.findAll(ResumeSpecification.candidateResumeFilter(candidate.getId(), keyword, type), Sort.by(Sort.Direction.DESC, "id"));
        return resumes.stream().map(resumeMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeDetailResponse getResumeDetail(Integer resumeId) {
        Role currentRole = JwtTokenProvider.getCurrentRole();
        Resume resume;
        if (currentRole == Role.CANDIDATE) {
            resume = getOwnedResume(resumeId);
            if (Boolean.TRUE.equals(resume.getIsDeleted())) {
                throw new AppException(ErrorCode.RESUME_NOT_EXISTED);
            }
        } else if (currentRole == Role.RECRUITER || currentRole == Role.ADMIN) {
            resume = resumeRepository.findById(resumeId)
                                     .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_EXISTED));
        } else {
            throw new AppException(ErrorCode.NOT_HAVE_PERMISSION);
        }
        return resumeDetailMapper.toDetailResponse(resume);
    }

    @Override
    public ResumeResponse uploadResume(UploadResumeRequest request) {
        quotaService.validateStateQuota(FeatureKey.CV_UPLOAD_LIMIT, null);
        if (!hasSupportedResumeExtension(request)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Only PDF, DOC, and DOCX files are supported");
        }

        Resume resume = resumeMapper.toEntity(request);
        Candidate candidate = getCurrentCandidate();

        resume.setType(ResumeType.ORIGINAL);
        resume.setRootResume(null);
        resume.setStatus(ResumeStatus.DRAFT);
        resume.setParseStatus(ResumeParseStatus.WAITING);
        resume.setIsDeleted(Boolean.FALSE);
        if (resume.getIsDefault() == null) {
            resume.setIsDefault(Boolean.FALSE);
        }

        resume.setCandidate(candidate);
        resume = resumeRepository.save(resume);

        return resumeMapper.toResponse(resume);
    }

    @Override
    public ResumeResponse parseResume(Integer resumeId) {
        // Get resume first to have the ID for context
        Resume resume = getOwnedResume(resumeId);

        // Consume quota with resume as context
        quotaService.consumeEventQuota(
                FeatureKey.RESUME_PARSING,
                1,
                UsageEntityType.RESUME,
                resume.getId()
        );

        resume.setStatus(ResumeStatus.DRAFT);
        resume.setParseStatus(ResumeParseStatus.WAITING);
        resume = resumeRepository.save(resume);

        try {
            resumeParsingRequestPublisher.publish(resume.getId(), resume.getResumeUrl(), resume.getFileName(), resume.getResumeName());
            resume.setParseStatus(ResumeParseStatus.PARTIAL);
            resumeRepository.save(resume);
        } catch (Exception e) {
            log.error("Failed to enqueue resume parsing request for resumeId={}", resumeId, e);
            resume.setParseStatus(ResumeParseStatus.FAIL);
            resumeRepository.save(resume);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return resumeMapper.toResponse(resume);
    }

    @Override
    public ResumeResponse updateResume(Integer resumeId, UpdateResumeRequest request) {
        Resume resume = getOwnedResume(resumeId);
        resumeMapper.updateFromRequest(request, resume);

        resumeRepository.save(resume);
        return resumeMapper.toResponse(resume);
    }

    @Override
    public ResumeResponse cloneResume(Integer resumeId, ResumeType targetType) {
        if (targetType == null) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        Resume source = getOwnedResume(resumeId);
        if (targetType == ResumeType.PROFILE && source.getType() == ResumeType.PROFILE) {
            return resumeMapper.toResponse(source);
        }

        if (targetType == ResumeType.PROFILE && source.getParseStatus() != ResumeParseStatus.FINISH) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Only parsed resumes can be set as profile");
        }

        if (targetType == ResumeType.PROFILE) {
            Resume existingProfile = resumeRepository
                    .findFirstByCandidate_IdAndTypeOrderByIdDesc(source.getCandidate().getId(), ResumeType.PROFILE)
                    .orElse(null);
            if (existingProfile != null && !existingProfile.getId().equals(source.getId())) {
                resumeRepository.delete(existingProfile);
                resumeRepository.flush();
            }
        }

        Resume clone = resumeMapper.cloneEntity(source, source, targetType);

        resumeCloneService.cloneAll(source, clone);

        Resume saved = resumeRepository.save(clone);
        return resumeMapper.toResponse(saved);
    }

    @Override
    public String getResumeStatus(Integer resumeId) {
        Resume resume = getOwnedResume(resumeId);
        if (Boolean.TRUE.equals(resume.getIsDeleted())) {
            throw new AppException(ErrorCode.RESUME_NOT_EXISTED);
        }
        if (resume.getStatus() == null) {
            return ResumeStatus.DRAFT.name();
        }
        return resume.getStatus().name();
    }

    @Override
    public String getResumeParseStatus(Integer resumeId) {
        Resume resume = getOwnedResume(resumeId);
        if (Boolean.TRUE.equals(resume.getIsDeleted())) {
            throw new AppException(ErrorCode.RESUME_NOT_EXISTED);
        }
        if (resume.getParseStatus() == null) {
            return ResumeParseStatus.WAITING.name();
        }
        return resume.getParseStatus().name();
    }

    @Override
    public void deleteResume(Integer resumeId) {
        Resume resume = getOwnedResume(resumeId);

        if (applicationRepository.existsByResume_Id(resumeId) || resumeRepository.existsByRootResume_Id(resumeId)) {
            resume.setIsDeleted(Boolean.TRUE);
            resumeRepository.save(resume);
            return;
        }

        resumeRepository.delete(resume);
        resumeRepository.flush();
    }

    @Override
    public ResumeResponse createResumeBuilder() {
        Candidate candidate = candidateRepository.findById(JwtTokenProvider.getCurrentCandidateId())
                                                 .orElseThrow(() -> new AppException(ErrorCode.CANDIDATE_NOT_EXISTED));
        Resume resume = Resume.builder()
                .type(ResumeType.TEMPLATE)
                .status(ResumeStatus.ACTIVE)
                .isDeleted(Boolean.FALSE)
                .candidate(candidate)
                .build();
        resume = resumeRepository.save(resume);
        return resumeMapper.toResponse(resume);
    }

    private Candidate getCurrentCandidate() {
        return candidateRepository.findById(JwtTokenProvider.getCurrentCandidateId())
                                  .orElseThrow(() -> new AppException(ErrorCode.CANDIDATE_NOT_EXISTED));
    }

    private Resume getOwnedResume(Integer resumeId) {
        Candidate candidate = getCurrentCandidate();
        Resume resume = resumeRepository.findById(resumeId)
                                        .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_EXISTED));

        if (resume.getCandidate() == null || !resume.getCandidate().getId().equals(candidate.getId())) {
            throw new AppException(ErrorCode.NOT_HAVE_PERMISSION);
        }

        return resume;
    }

    private boolean hasSupportedResumeExtension(UploadResumeRequest request) {
        if (request == null) {
            return false;
        }

        String valueToCheck = null;
        if (StringUtils.hasText(request.getFileName())) {
            valueToCheck = request.getFileName();
        } else if (StringUtils.hasText(request.getResumeName())) {
            valueToCheck = request.getResumeName();
        } else if (StringUtils.hasText(request.getResumeUrl())) {
            valueToCheck = request.getResumeUrl();
        }

        if (!StringUtils.hasText(valueToCheck)) {
            return false;
        }

        String normalized = valueToCheck.trim().toLowerCase(Locale.ROOT);
        int queryIndex = normalized.indexOf('?');
        if (queryIndex >= 0) {
            normalized = normalized.substring(0, queryIndex);
        }

        return normalized.endsWith(".pdf")
                || normalized.endsWith(".doc")
                || normalized.endsWith(".docx");
    }

}
