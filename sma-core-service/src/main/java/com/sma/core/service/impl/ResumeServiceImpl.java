package com.sma.core.service.impl;

import com.sma.core.dto.request.resume.UpdateResumeRequest;
import com.sma.core.dto.request.resume.UploadResumeRequest;
import com.sma.core.dto.response.resume.ResumeDetailResponse;
import com.sma.core.dto.response.resume.ResumeResponse;
import com.sma.core.entity.Candidate;
import com.sma.core.entity.Resume;
import com.sma.core.enums.ResumeParseStatus;
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
import com.sma.core.specification.ResumeSpecification;
import com.sma.core.utils.JwtTokenProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        Resume resume = resumeMapper.toEntity(request);
        Candidate candidate = getCurrentCandidate();

        resume.setType(ResumeType.ORIGINAL);
        resume.setRootResume(null);
        resume.setStatus(ResumeStatus.DRAFT);
        resume.setParseStatus(ResumeParseStatus.WAITING);
        if (resume.getIsDefault() == null) {
            resume.setIsDefault(Boolean.FALSE);
        }
        if (resume.getIsOverrided() == null) {
            resume.setIsOverrided(Boolean.FALSE);
        }

        resume.setCandidate(candidate);
        resume = resumeRepository.save(resume);

        try {
            resumeParsingRequestPublisher.publish(resume.getId(), resume.getResumeUrl(), resume.getFileName(), resume.getResumeName());
            resume.setParseStatus(ResumeParseStatus.PARTIAL);
            resumeRepository.save(resume);
        } catch (Exception e) {
            log.error("Failed to enqueue resume parsing request for resumeId={}", resume.getId(), e);
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
    public ResumeResponse reparseResume(Integer resumeId) {
        Resume resume = getOwnedResume(resumeId);
        resume.setStatus(ResumeStatus.DRAFT);
        resume.setParseStatus(ResumeParseStatus.WAITING);
        resume = resumeRepository.save(resume);

        try {
            resumeParsingRequestPublisher.publish(resume.getId(), resume.getResumeUrl(), resume.getFileName(), resume.getResumeName());
            resume.setParseStatus(ResumeParseStatus.PARTIAL);
            resumeRepository.save(resume);
        } catch (Exception e) {
            log.error("Failed to re-enqueue resume parsing request for resumeId={}", resumeId, e);
            resume.setParseStatus(ResumeParseStatus.FAIL);
            resumeRepository.save(resume);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return resumeMapper.toResponse(resume);
    }

    @Override
    public String getResumeStatus(Integer resumeId) {
        Resume resume = getOwnedResume(resumeId);
        if (resume.getStatus() == null) {
            return ResumeStatus.DRAFT.name();
        }
        return resume.getStatus().name();
    }

    @Override
    public String getResumeParseStatus(Integer resumeId) {
        Resume resume = getOwnedResume(resumeId);
        if (resume.getParseStatus() == null) {
            return ResumeParseStatus.WAITING.name();
        }
        return resume.getParseStatus().name();
    }

    @Override
    public void deleteResume(Integer resumeId) {
        Resume resume = getOwnedResume(resumeId);

        if (applicationRepository.existsByResume_Id(resumeId) || resumeRepository.existsByRootResume_Id(resumeId)) {
            throw new AppException(ErrorCode.CANT_DELETE_RESUME_IN_USE);
        }

        try {
            resumeRepository.delete(resume);
            resumeRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            log.warn("Cannot delete resumeId={} due to FK constraints", resumeId, ex);
            throw new AppException(ErrorCode.CANT_DELETE_RESUME_IN_USE);
        }
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
}
