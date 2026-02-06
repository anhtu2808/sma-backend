package com.sma.core.service.impl;

import com.sma.core.dto.request.job.*;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.job.BaseJobResponse;
import com.sma.core.dto.response.job.JobDetailResponse;
import com.sma.core.entity.*;
import com.sma.core.enums.JobStatus;
import com.sma.core.enums.Role;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.job.JobMapper;
import com.sma.core.repository.*;
import com.sma.core.service.*;
import com.sma.core.specification.JobSpecification;
import com.sma.core.utils.JwtTokenProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    final JobRepository jobRepository;
    final JobMapper jobMapper;
    final JobExpertiseRepository jobExpertiseRepository;
    final SkillRepository skillRepository;
    final RecruiterRepository recruiterRepository;
    final DomainRepository domainRepository;
    final BenefitRepository benefitRepository;
    final JobQuestionRepository jobQuestionRepository;
    final ScoringCriteriaService scoringCriteriaService;

    @Override
    public JobDetailResponse getJobById(Integer id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));
        // handle restrict candidate access to SUSPENDED, DRAFT, PENDING REVIEW job
        Role role = JwtTokenProvider.getCurrentRole();
        if (role == null || role.equals(Role.CANDIDATE)) {
            EnumSet<JobStatus> allowedStatus = EnumSet.of(JobStatus.PUBLISHED, JobStatus.CLOSED);
            if (!allowedStatus.contains(job.getStatus()))
                throw new AppException(ErrorCode.JOB_NOT_AVAILABLE);
            return jobMapper.toJobDetailResponse(job);
        }
        return jobMapper.toJobInternalResponse(job);
    }

    @Override
    @Transactional
    public void closeExpiredJob() {
        LocalDateTime now = LocalDateTime.now();
        List<Job> expiredJob = jobRepository.findByExpDateBeforeAndStatus(now, JobStatus.PUBLISHED);
        expiredJob.forEach(job -> job.setStatus(JobStatus.CLOSED));
        jobRepository.saveAll(expiredJob);
    }

    @Override
    public JobDetailResponse draftJob(DraftJobRequest request) {
        Recruiter recruiter = recruiterRepository.findById(JwtTokenProvider.getCurrentRecruiterId())
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED));
        Job job = jobMapper.toJob(request);

        if (request.getExpertiseId() != null) {
            job.setExpertise(jobExpertiseRepository.getReferenceById(request.getExpertiseId()));
        }
        if (request.getSkillIds() != null && !request.getSkillIds().isEmpty()) {
            List<Skill> skillSet = skillRepository.findAllById(request.getSkillIds());
            job.setSkills(Set.copyOf(skillSet));
        }
        if (request.getDomainIds() != null && !request.getDomainIds().isEmpty()) {
            List<Domain> domains = domainRepository.findAllById(request.getDomainIds());
            job.setDomains(Set.copyOf(domains));
        }
        if (request.getBenefitIds() != null && !request.getBenefitIds().isEmpty()) {
            List<Benefit> benefits = benefitRepository.findAllById(request.getBenefitIds());
            job.setBenefits(Set.copyOf(benefits));
        }
        if (request.getQuestionIds() != null && !request.getQuestionIds().isEmpty()) {
            List<JobQuestion> jobQuestions = jobQuestionRepository.findAllById(request.getQuestionIds());
            job.setQuestions(Set.copyOf(jobQuestions));
        }
        if (request.getScoringCriteriaIds() != null && !request.getScoringCriteriaIds().isEmpty()) {
            Set<ScoringCriteria> scoringCriterias = scoringCriteriaService
                    .saveJobScoringCriteria(request.getScoringCriteriaIds());
            scoringCriterias
                    .forEach(scoringCriteria -> scoringCriteria.setJob(job));
            job.setScoringCriterias(scoringCriterias);
        }
        job.setUploadTime(LocalDateTime.now());
        job.setCompany(recruiter.getCompany());
        job.setStatus(JobStatus.DRAFT);
        jobRepository.save(job);
        return jobMapper.toJobInternalResponse(job);
    }

    @Override
    public JobDetailResponse publishExistingJob(Integer id, PublishJobRequest request) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));
        EnumSet<JobStatus> allowedStatus = EnumSet.of(JobStatus.DRAFT, JobStatus.CLOSED);
        if (request.getExpertiseId() != null && !request.getExpertiseId().equals(job.getExpertise().getId())) {
            job.setExpertise(jobExpertiseRepository.getReferenceById(request.getExpertiseId()));
        }
        if (!allowedStatus.contains(job.getStatus()))
            throw new AppException(ErrorCode.CAN_NOT_PUBLISH);
        job = jobMapper.toJob(request, job);
        if (request.getSkillIds() != null && !request.getSkillIds().isEmpty()) {
            Set<Integer> newSkillIds = syncAndFilterNewIds(
                    job.getSkills(),
                    Set.copyOf(request.getSkillIds()),
                    Skill::getId);
            Set<Skill> newSkills = newSkillIds.stream()
                    .map(skillRepository::getReferenceById)
                    .collect(Collectors.toSet());
            job.getSkills().addAll(newSkills);
        }
        if (request.getDomainIds() != null && !request.getDomainIds().isEmpty()) {
            Set<Integer> newIds = syncAndFilterNewIds(
                    job.getDomains(),
                    Set.copyOf(request.getDomainIds()),
                    Domain::getId);
            Set<Domain> newDomains = newIds.stream()
                    .map(domainRepository::getReferenceById)
                    .collect(Collectors.toSet());
            job.getDomains().addAll(newDomains);
        }
        if (request.getBenefitIds() != null && !request.getBenefitIds().isEmpty()) {
            Set<Integer> newIds = syncAndFilterNewIds(
                    job.getBenefits(),
                    Set.copyOf(request.getBenefitIds()),
                    Benefit::getId);
            Set<Benefit> newBenefits = newIds.stream()
                    .map(benefitRepository::getReferenceById)
                    .collect(Collectors.toSet());
            job.getBenefits().addAll(newBenefits);
        }
        if (request.getQuestionIds() != null && !request.getQuestionIds().isEmpty()) {
            Set<Integer> newIds = syncAndFilterNewIds(
                    job.getQuestions(),
                    Set.copyOf(request.getQuestionIds()),
                    JobQuestion::getId);
            Set<JobQuestion> newQuestions = newIds.stream()
                    .map(jobQuestionRepository::getReferenceById)
                    .collect(Collectors.toSet());
            job.getQuestions().addAll(newQuestions);
        }

        if (request.getScoringCriterias() != null && !request.getScoringCriterias().isEmpty()) {
            job.setScoringCriterias(scoringCriteriaService
                    .saveJobScoringCriteria(job, Set.copyOf(request.getScoringCriterias())));
        }
        job.setStatus(JobStatus.PENDING_REVIEW);
        job.setUploadTime(LocalDateTime.now());
        jobRepository.save(job);
        return jobMapper.toJobInternalResponse(job);
    }

    @Override
    public JobDetailResponse updateJobStatus(Integer id, UpdateJobStatusRequest request) {
        Recruiter recruiter = recruiterRepository.findById(JwtTokenProvider.getCurrentRecruiterId())
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED));
        Job job = jobRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));
        boolean sameCompany =
                recruiter.getCompany().getId().equals(job.getCompany().getId());
        boolean isRoot = Boolean.TRUE.equals(recruiter.getIsRootRecruiter());
        if (!sameCompany || !isRoot) {
            throw new AppException(ErrorCode.NOT_HAVE_PERMISSION);
        }
        Role role = JwtTokenProvider.getCurrentRole();
        if (role == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        JobStatus currentStatus = job.getStatus();
        JobStatus targetStatus = request.getJobStatus();
        if (role != Role.ADMIN) {
            if (targetStatus == JobStatus.PUBLISHED
                    || targetStatus == JobStatus.SUSPENDED) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }
        switch (targetStatus) {
            case CLOSED -> {
                if (currentStatus != JobStatus.PUBLISHED) {
                    throw new AppException(ErrorCode.CAN_NOT_CLOSED);
                }
                job.setStatus(JobStatus.CLOSED);
            }
            case DRAFT -> {
                if (currentStatus != JobStatus.PENDING_REVIEW
                        && currentStatus != JobStatus.CLOSED) {
                    throw new AppException(ErrorCode.CAN_NOT_DRAFTED);
                }
                job.setStatus(JobStatus.DRAFT);
            }
            case PUBLISHED, SUSPENDED -> {
                    job.setStatus(targetStatus);
            }
            case PENDING_REVIEW -> {
                throw new AppException(ErrorCode.CAN_NOT_CHANGE_DIRECT_TO_PENDING);
            }
            default -> throw new AppException(ErrorCode.INVALID_JOB_STATUS);
        }
        jobRepository.save(job);
        return jobMapper.toJobDetailResponse(job);
    }

    /**
     * Get all job on job board page
     */
    @Override
    public PagingResponse<BaseJobResponse> getAllJob(JobFilterRequest request) {
        Role role = JwtTokenProvider.getCurrentRole();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        EnumSet<JobStatus> allowedStatus = null;
        LocalDateTime date = null;
        if (role == null || role.equals(Role.CANDIDATE)) {
            allowedStatus = EnumSet.of(JobStatus.PUBLISHED);
            date = LocalDateTime.now();
        } else if (role.equals(Role.RECRUITER) || role.equals(Role.ADMIN)) {
            if (request.getStatuses() != null && !request.getStatuses().isEmpty())
                allowedStatus = request.getStatuses();
            else
                allowedStatus = EnumSet.noneOf(JobStatus.class);
            if (role.equals(Role.RECRUITER)) {
                Recruiter recruiter = recruiterRepository.getReferenceById(JwtTokenProvider.getCurrentRecruiterId());
                request.setCompanyId(recruiter.getCompany().getId());
            }
        }
        return PagingResponse
                .fromPage(jobRepository.findAll(JobSpecification.withFilter(request, allowedStatus, date), pageable)
                        .map(jobMapper::toBaseJobResponse));
    }

    /**
     * Get all saved job of current user on candidate dashboard
     */
    @Override
    public PagingResponse<BaseJobResponse> getAllMySavedJob() {
        return null;
    }

    public <E, ID> Set<ID> syncAndFilterNewIds(
            Set<E> existingEntities,
            Set<ID> requestIds,
            Function<E, ID> entityIdExtractor) {

        existingEntities.removeIf(entity -> {
            ID id = entityIdExtractor.apply(entity);
            return id != null && !requestIds.contains(id);
        });

        Set<ID> remainingIds = existingEntities.stream()
                .map(entityIdExtractor)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return requestIds.stream()
                .filter(id -> id != null && !remainingIds.contains(id))
                .collect(Collectors.toSet());
    }

}
