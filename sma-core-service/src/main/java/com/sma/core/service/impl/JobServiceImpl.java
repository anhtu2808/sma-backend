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
    final UserRepository userRepository;
    final JobMarkRepository jobMarkRepository;
    final ApplicationRepository applicationRepository;

    @Override
    public JobDetailResponse getJobById(Integer id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));

        Role role = JwtTokenProvider.getCurrentRole();
        Integer currentCandidateId = JwtTokenProvider.getCurrentCandidateId();
        if (role == null || role.equals(Role.CANDIDATE)) {
            EnumSet<JobStatus> allowedStatus = EnumSet.of(JobStatus.PUBLISHED, JobStatus.CLOSED);
            if (!allowedStatus.contains(job.getStatus())) {
                throw new AppException(ErrorCode.JOB_NOT_AVAILABLE);
            }
            JobDetailResponse response = jobMapper.toJobDetailResponse(job);
            if (currentCandidateId != null) {
                long totalAttempts = applicationRepository.countByCandidateIdAndJobId(currentCandidateId, id);
                response.setAppliedAttempt((int) totalAttempts);
                applicationRepository.findFirstByCandidateIdAndJobIdOrderByAppliedAtDesc(currentCandidateId, id)
                        .ifPresent(lastApp -> {
                            response.setLastApplicationStatus(lastApp.getStatus());
                        });
                response.setCanApply(totalAttempts < 2);
            }
            return response;
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
    public JobDetailResponse saveJob(DraftJobRequest request) {
        return jobMapper.toJobInternalResponse(bindJobRelations(
                jobMapper.toJob(request),
                request.getExpertiseId(),
                request.getSkillIds(),
                request.getDomainIds(),
                request.getBenefitIds(),
                request.getQuestionIds(),
                request.getScoringCriterias(),
                null,
                request.getRootId(),
                true));
    }

    @Override
    public JobDetailResponse saveExistingJob(Integer id, DraftJobRequest request) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));
        return jobMapper.toJobInternalResponse(bindJobRelations(
                jobMapper.toJob(request, job),
                request.getExpertiseId(),
                request.getSkillIds(),
                request.getDomainIds(),
                request.getBenefitIds(),
                request.getQuestionIds(),
                request.getScoringCriterias(),
                id,
                request.getRootId(),
                true));
    }

    @Override
    public JobDetailResponse publishExistingJob(Integer id, PublishJobRequest request) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));
        EnumSet<JobStatus> allowedStatus = EnumSet.of(JobStatus.DRAFT, JobStatus.CLOSED);
        if (!allowedStatus.contains(job.getStatus()))
            throw new AppException(ErrorCode.CAN_NOT_PUBLISH);
        return jobMapper.toJobInternalResponse(bindJobRelations(
                jobMapper.toJob(request, job),
                request.getExpertiseId(),
                request.getSkillIds(),
                request.getDomainIds(),
                request.getBenefitIds(),
                request.getQuestionIds(),
                request.getScoringCriterias(),
                id,
                request.getRootId(),
                false));
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

    @Override
    public Boolean markJob(Integer jobId) {
        Integer userId = JwtTokenProvider.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));
        JobMark jobMark = jobMarkRepository.findByUser_IdAndJob_Id(userId, jobId)
                .orElse(null);
        if (jobMark != null) {
            jobMarkRepository.delete(jobMark);
            return false;
        }
        jobMarkRepository.save(JobMark.builder()
                .user(user)
                .job(job)
                .build());
        return true;
    }

    /**
     * Get all saved job of current user on candidate dashboard
     */
    @Override
    public PagingResponse<BaseJobResponse> getAllMyFavoriteJob(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Integer userId = JwtTokenProvider.getCurrentUserId();
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        Page<JobMark> jobMarks = jobMarkRepository.findByUser_Id(userId, pageable);
        Page<Job> jobs = jobMarks.map(JobMark::getJob);
        return PagingResponse.fromPage(jobs.map(jobMapper::toBaseJobResponse));
    }

    Job bindJobRelations(Job job,
                         Integer expertiseId,
                         List<Integer> skillIds,
                         List<Integer> domainIds,
                         List<Integer> benefitIds,
                         List<Integer> questionIds,
                         Set<AddJobScoringCriteriaRequest> scoringCriteriaRequests,
                         Integer jobId,
                         Integer rootId,
                         boolean isSaved) {
        Recruiter recruiter = recruiterRepository.findById(JwtTokenProvider.getCurrentRecruiterId())
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED));
        if (jobId != null){
            if (!recruiter.getCompany().getId().equals(job.getCompany().getId())) {
                throw new AppException(ErrorCode.NOT_HAVE_PERMISSION);
            }
        }
        if (expertiseId != null) {
            job.setExpertise(jobExpertiseRepository.getReferenceById(expertiseId));
        }
        if (skillIds != null && !skillIds.isEmpty()) {
            Set<Skill> skillSet =
                    new HashSet<>(skillRepository.findAllById(skillIds));
            job.setSkills(skillSet);
        }
        if (domainIds != null && !domainIds.isEmpty()) {
            List<Domain> domains = domainRepository.findAllById(domainIds);
            job.setDomains(new HashSet<>(domains));
        }

        if (benefitIds != null && !benefitIds.isEmpty()) {
            List<Benefit> benefits = benefitRepository.findAllById(benefitIds);
            job.setBenefits(new HashSet<>(benefits));
        }

        if (questionIds != null && !questionIds.isEmpty()) {
            List<JobQuestion> jobQuestions = jobQuestionRepository.findAllById(questionIds);
            job.setQuestions(new HashSet<>(jobQuestions));
        }

        if (scoringCriteriaRequests != null && !scoringCriteriaRequests.isEmpty()) {
            job.setScoringCriterias(scoringCriteriaService
                    .saveJobScoringCriteria(job, scoringCriteriaRequests));
        }
        if (rootId != null) {
            Job rootJob = jobRepository.findById(rootId)
                    .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));
            rootJob.setStatus(JobStatus.CLOSED);
            jobRepository.save(rootJob);
            job.setRootJob(rootJob);
        }
        if (isSaved){
            job.setStatus(JobStatus.DRAFT);
        } else {
            job.setStatus(JobStatus.PENDING_REVIEW);
        }
        job.setCompany(recruiter.getCompany());
        job.setUploadTime(LocalDateTime.now());
        return jobRepository.save(job);
    }

}
