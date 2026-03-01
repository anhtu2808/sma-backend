package com.sma.core.service.impl;

import com.sma.core.dto.request.job.*;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.job.BaseJobResponse;
import com.sma.core.dto.response.job.JobDetailResponse;
import com.sma.core.entity.*;
import com.sma.core.enums.JobStatus;
import com.sma.core.enums.Role;
import com.sma.core.enums.SubscriptionStatus;
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
import org.springframework.data.domain.PageImpl;
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
    private static final Comparator<Application> LATEST_APPLICATION_COMPARATOR = Comparator
            .comparing(Application::getAttempt, Comparator.nullsFirst(Integer::compareTo))
            .thenComparing(Application::getAppliedAt, Comparator.nullsFirst(LocalDateTime::compareTo));
    private static final Comparator<Application> APPLIED_JOB_PAGE_SORT_COMPARATOR = (current, next) -> {
        int comparedAppliedAt = Comparator.nullsFirst(LocalDateTime::compareTo).compare(next.getAppliedAt(), current.getAppliedAt());
        if (comparedAppliedAt != 0) return comparedAppliedAt;
        return Comparator.nullsFirst(Integer::compareTo).compare(next.getAttempt(), current.getAttempt());
    };

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
    final BannedKeywordServiceImpl bannedKeywordService;
    final SubscriptionRepository subscriptionRepository;
    final UsageEventRepository usageEventRepository;
    final CompanyLocationRepository companyLocationRepository;

    @Override
    public JobDetailResponse getJobById(Integer id) {
        Job job = jobRepository.findById(id)
                               .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));

        Role role = JwtTokenProvider.getCurrentRole();
        if (role == null || role.equals(Role.CANDIDATE)) {
            Integer currentCandidateId = JwtTokenProvider.getCurrentCandidateId();
            EnumSet<JobStatus> allowedStatus = EnumSet.of(JobStatus.PUBLISHED, JobStatus.CLOSED);
            if (!allowedStatus.contains(job.getStatus())) {
                throw new AppException(ErrorCode.JOB_NOT_AVAILABLE);
            }
            JobDetailResponse response = jobMapper.toJobDetailResponse(job);
            if (Boolean.TRUE.equals(job.getIsSample())) {
                response.setCanApply(false);
                response.setAppliedAttempt(0);
                return response;
            }
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
                request.getLocationIds(),
                true, false));
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
                request.getLocationIds(),
                true, false));
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
                request.getLocationIds(),
                false, false));
    }

    @Override
    @Transactional
    public JobDetailResponse updateJobStatus(Integer id, UpdateJobStatusRequest request) {
        Job job = jobRepository.findById(id)
                               .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));

        Role role = JwtTokenProvider.getCurrentRole();
        if (role == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (role == Role.RECRUITER) {
            Integer currentRecruiterId = JwtTokenProvider.getCurrentRecruiterId();
            Recruiter recruiter = recruiterRepository.findById(currentRecruiterId)
                                                     .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED));

            boolean sameCompany = recruiter.getCompany().getId().equals(job.getCompany().getId());
            boolean isRoot = Boolean.TRUE.equals(recruiter.getIsRootRecruiter());

            if (!sameCompany || !isRoot) {
                throw new AppException(ErrorCode.NOT_HAVE_PERMISSION);
            }
            if (request.getJobStatus() == JobStatus.PUBLISHED || request.getJobStatus() == JobStatus.SUSPENDED || job.getStatus().equals(JobStatus.SUSPENDED)) {
                throw new AppException(ErrorCode.NOT_HAVE_PERMISSION);
            }
        }
        JobStatus currentStatus = job.getStatus();
        JobStatus targetStatus = request.getJobStatus();

        switch (targetStatus) {
            case CLOSED -> {
                if (currentStatus != JobStatus.PUBLISHED) throw new AppException(ErrorCode.CAN_NOT_CLOSED);
                job.setStatus(JobStatus.CLOSED);
            }
            case DRAFT -> {
                if (currentStatus != JobStatus.PENDING_REVIEW && currentStatus != JobStatus.CLOSED)
                    throw new AppException(ErrorCode.CAN_NOT_DRAFTED);
                job.setStatus(JobStatus.DRAFT);
            }
            case PUBLISHED, SUSPENDED -> {
                job.setStatus(targetStatus);
            }
            case PENDING_REVIEW -> throw new AppException(ErrorCode.CAN_NOT_CHANGE_DIRECT_TO_PENDING);
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
        Map<Integer, Application> latestApplicationsByJobId = Collections.emptyMap();
        if (role == null || role.equals(Role.CANDIDATE)) {
            allowedStatus = EnumSet.of(JobStatus.PUBLISHED);
            date = LocalDateTime.now();
            request.setIsSample(false);
            if (role != null ){
                Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
                if (candidateId != null) {
                    latestApplicationsByJobId = getLatestApplicationsByJobId(candidateId);
                }
            }
        } else if (role.equals(Role.RECRUITER) || role.equals(Role.ADMIN)) {
            if (request.getStatus() != null && !request.getStatus().isEmpty())
                allowedStatus = request.getStatus();
            else
                allowedStatus = EnumSet.noneOf(JobStatus.class);
            if (role.equals(Role.RECRUITER)) {
                Recruiter recruiter = recruiterRepository.getReferenceById(JwtTokenProvider.getCurrentRecruiterId());
                request.setCompanyId(recruiter.getCompany().getId());
                request.setIsSample(false);
            }
        }
        Page<Job> jobPage = jobRepository.findAll(JobSpecification.withFilter(request, allowedStatus, date), pageable);
        List<BaseJobResponse> jobResponses = jobPage.getContent().stream().map(jobMapper::toBaseJobResponse).toList();
        enrichApplyInfo(jobResponses, latestApplicationsByJobId);
        return PagingResponse.fromPage(jobPage, jobResponses);
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
        List<BaseJobResponse> jobResponses = jobs.getContent().stream().map(jobMapper::toBaseJobResponse).toList();
        Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
        Map<Integer, Application> latestApplicationsByJobId = candidateId == null
                ? Collections.emptyMap()
                : getLatestApplicationsByJobId(candidateId);
        enrichApplyInfo(jobResponses, latestApplicationsByJobId);
        return PagingResponse.fromPage(jobs, jobResponses);
    }

    @Override
    public PagingResponse<BaseJobResponse> getAllMyAppliedJob(Integer page, Integer size) {
        Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
        if (candidateId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Pageable pageable = PageRequest.of(page, size);
        Map<Integer, Application> latestApplicationsByJobId = getLatestApplicationsByJobId(candidateId);
        List<Application> latestApplications = latestApplicationsByJobId.values()
                                                                        .stream()
                                                                        .sorted(APPLIED_JOB_PAGE_SORT_COMPARATOR)
                                                                        .toList();

        int start = Math.min((int) pageable.getOffset(), latestApplications.size());
        int end = Math.min(start + pageable.getPageSize(), latestApplications.size());

        List<Job> pagedJobs = latestApplications.subList(start, end)
                                                .stream()
                                                .map(Application::getJob)
                                                .toList();
        Page<Job> jobPage = new PageImpl<>(pagedJobs, pageable, latestApplications.size());

        List<BaseJobResponse> jobResponses = pagedJobs.stream().map(jobMapper::toBaseJobResponse).toList();
        enrichApplyInfo(jobResponses, latestApplicationsByJobId);
        return PagingResponse.fromPage(jobPage, jobResponses);
    }

    private Map<Integer, Application> getLatestApplicationsByJobId(Integer candidateId) {
        return applicationRepository.findByCandidate_Id(candidateId).stream()
                                    .filter(app -> app.getJob() != null && app.getJob().getId() != null)
                                    .collect(Collectors.toMap(
                                            app -> app.getJob().getId(),
                                            Function.identity(),
                                            (current, next) -> LATEST_APPLICATION_COMPARATOR.compare(next, current) > 0 ? next : current
                                    ));
    }

    private void enrichApplyInfo(List<BaseJobResponse> jobResponses, Map<Integer, Application> latestApplicationsByJobId) {
        if (latestApplicationsByJobId == null || latestApplicationsByJobId.isEmpty()) return;
        for (BaseJobResponse jobResponse : jobResponses) {
            Application latestApplication = latestApplicationsByJobId.get(jobResponse.getId());
            if (latestApplication != null) {
                jobResponse.setIsApplied(true);
                jobResponse.setLastApplyAt(latestApplication.getAppliedAt());
                jobResponse.setApplicationStatus(latestApplication.getStatus());
                jobResponse.setAppliedResumeUrl(
                        latestApplication.getResume() != null ? latestApplication.getResume().getResumeUrl() : null
                );
            }
        }
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
                         List<Integer> locationIds,
                         boolean isSaved,
                         boolean isSample) {
        if (!isSample) {
            Recruiter recruiter = recruiterRepository.findById(JwtTokenProvider.getCurrentRecruiterId())
                    .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED));

            if (jobId != null) {
                if (!recruiter.getCompany().getId().equals(job.getCompany().getId())) {
                    throw new AppException(ErrorCode.NOT_HAVE_PERMISSION);
                }
            }

            job.setCompany(recruiter.getCompany());
            job.setIsSample(false);
            if (locationIds != null && !locationIds.isEmpty()) {
                Set<CompanyLocation> locations = new HashSet<>();
                for (Integer locationId : locationIds) {
                    CompanyLocation location = companyLocationRepository
                            .findByIdAndCompanyId(locationId, recruiter.getCompany().getId())
                            .orElseThrow(() -> new AppException(ErrorCode.COMPANY_LOCATION_NOT_FOUND));
                    locations.add(location);
                }
                job.setLocations(locations);
            }
        } else {
            job.setCompany(null);
            job.setIsSample(true);
            job.setLocations(null);
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

        if (!isSample && !isSaved && Boolean.TRUE.equals(job.getEnableAiScoring())) {
            validateAndCheckAiQuota(job, job.getEnableAiScoring(), job.getAutoRejectThreshold());
        }
        if (rootId != null) {
            Job rootJob = jobRepository.findById(rootId)
                                       .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));
            rootJob.setStatus(JobStatus.CLOSED);
            jobRepository.save(rootJob);
            job.setRootJob(rootJob);
        }
        if (isSample) {
            job.setStatus(JobStatus.PUBLISHED);
        } else {
            boolean hasViolation = bannedKeywordService.isContentViolated(job);
            job.setIsViolated(hasViolation);
            if (isSaved) {
                job.setStatus(JobStatus.DRAFT);
            } else {
                job.setStatus(hasViolation ? JobStatus.PENDING_REVIEW : JobStatus.PUBLISHED);
            }
        }

        job.setUploadTime(LocalDateTime.now());
        return jobRepository.save(job);
    }

    @Override
    public PagingResponse<BaseJobResponse> getJobsByCurrentCompany(JobFilterRequest request) {
        Integer currentRecruiterId = JwtTokenProvider.getCurrentRecruiterId();
        Recruiter recruiter = recruiterRepository.findById(currentRecruiterId)
                                                 .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED));

        request.setCompanyId(recruiter.getCompany().getId());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        EnumSet<JobStatus> allowedStatus;
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            allowedStatus = request.getStatus();
        } else {
            allowedStatus = EnumSet.noneOf(JobStatus.class);
        }
        Page<Job> jobPage = jobRepository.findAll(
                JobSpecification.withFilter(request, allowedStatus, null),
                pageable
        );

        return PagingResponse.fromPage(jobPage.map(jobMapper::toBaseJobResponse));
    }


    @Override
    @Transactional
    public JobDetailResponse updateAiSettings(Integer jobId, JobAiSettingsRequest request) {
        Job job = jobRepository.findById(jobId)
                               .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));

        verifyPermission(job);
        validateAndCheckAiQuota(job, request.getEnableAiScoring(), request.getAutoRejectThreshold());
        job.setEnableAiScoring(request.getEnableAiScoring());
        job.setAutoRejectThreshold(request.getAutoRejectThreshold());
        return jobMapper.toJobInternalResponse(jobRepository.save(job));
    }

    private void verifyPermission(Job job) {
        Role role = JwtTokenProvider.getCurrentRole();
        if (role == Role.ADMIN) return;

        if (role == Role.RECRUITER) {
            Integer currentRecruiterId = JwtTokenProvider.getCurrentRecruiterId();
            Recruiter recruiter = recruiterRepository.findById(currentRecruiterId)
                                                     .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED));

            if (!recruiter.getCompany().getId().equals(job.getCompany().getId())) {
                throw new AppException(ErrorCode.NOT_HAVE_PERMISSION);
            }
        } else {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void validateAndCheckAiQuota(Job job, Boolean enableAiScoring, Double threshold) {
        if (Boolean.TRUE.equals(enableAiScoring)) {
            if (job.getScoringCriterias() == null || job.getScoringCriterias().isEmpty()) {
                throw new AppException(ErrorCode.MISSING_SCORING_CRITERIA);
            }

            double totalWeight = job.getScoringCriterias().stream()
                                    .mapToDouble(ScoringCriteria::getWeight)
                                    .sum();

            if (Math.abs(totalWeight - 100.0) > 0.001) {
                throw new AppException(ErrorCode.INVALID_SCORING_WEIGHT);
            }
            List<Subscription> activeSubs = subscriptionRepository.findEligibleByCompanyId(
                    job.getCompany().getId(),
                    SubscriptionStatus.ACTIVE,
                    LocalDateTime.now()
            );

            if (activeSubs.isEmpty()) {
                throw new AppException(ErrorCode.NO_ACTIVE_SUBSCRIPTION);
            }

            Subscription sub = activeSubs.get(0);
            String AI_FEATURE_KEY = "AI_SCORING";

            UsageLimit aiLimit = sub.getPlan().getUsageLimits().stream()
                                    .filter(limit -> limit.getFeature().getFeatureKey().equals(AI_FEATURE_KEY))
                                    .findFirst()
                                    .orElseThrow(() -> new AppException(ErrorCode.FEATURE_NOT_SUPPORTED));

            Long usedAmount = usageEventRepository.sumTotal(sub.getId(), aiLimit.getFeature().getId());

            if (usedAmount >= aiLimit.getMaxQuota()) {
                throw new AppException(ErrorCode.AI_QUOTA_EXHAUSTED);
            }
        }
    }

    @Override
    @Transactional
    public JobDetailResponse createSampleJob(AdminJobSampleRequest request) {
        return jobMapper.toJobInternalResponse(bindJobRelations(
                jobMapper.toJob(request),
                request.getExpertiseId(), request.getSkillIds(),
                request.getDomainIds(), request.getBenefitIds(),
                request.getQuestionIds(), request.getScoringCriterias(),
                null,
                null,
                null,
                true, true
        ));
    }


    @Override
    public PagingResponse<BaseJobResponse> getSampleJobs(JobFilterRequest request) {
        // Ép buộc lọc theo Sample và Status Published
        request.setIsSample(true);
        EnumSet<JobStatus> allowedStatus = EnumSet.of(JobStatus.PUBLISHED);

        // Sử dụng trực tiếp request đã có các tham số lọc từ Controller
        return PagingResponse.fromPage(jobRepository.findAll(
                        JobSpecification.withFilter(request, allowedStatus, null),
                        PageRequest.of(request.getPage(), request.getSize()))
                .map(jobMapper::toBaseJobResponse));
    }

    @Override
    @Transactional
    public JobDetailResponse updateSampleJob(Integer id, AdminJobSampleRequest request) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));

        if (!Boolean.TRUE.equals(job.getIsSample())) {
            throw new AppException(ErrorCode.NOT_A_SAMPLE_JOB);
        }
        jobMapper.updateJobFromRequest(request, job);
        return jobMapper.toJobInternalResponse(bindJobRelations(
                job,
                request.getExpertiseId(), request.getSkillIds(),
                request.getDomainIds(), request.getBenefitIds(),
                request.getQuestionIds(), request.getScoringCriterias(),
                id,
                null,
                null,
                true, true
        ));
    }

    @Override
    @Transactional
    public void deleteSampleJob(Integer id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));

        if (!Boolean.TRUE.equals(job.getIsSample())) {
            throw new AppException(ErrorCode.CANNOT_DELETE_NON_SAMPLE);
        }

        jobRepository.delete(job);
    }

    @Override
    public JobDetailResponse updateJobExpiredDate(Integer id, UpdateJobExpDateRequest request) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));
        verifyPermission(job);
        if (request.getExpDate() != null) {
            if (request.getExpDate().isBefore(LocalDateTime.now())) {
                throw new AppException(ErrorCode.INVALID_EXPIRED_DATE);
            }
            job.setExpDate(request.getExpDate());
            jobRepository.save(job);
        }
        return jobMapper.toJobInternalResponse(job);
    }

    @Override
    public JobDetailResponse updateThreshold(Integer id, UpdateThresholdRequest request) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));
        verifyPermission(job);
        if (request.getScoringCriteria() != null && !request.getScoringCriteria().isEmpty()) {
            job.setScoringCriterias(scoringCriteriaService
                    .saveJobScoringCriteria(job, request.getScoringCriteria()));
            jobRepository.save(job);
        }
        return jobMapper.toJobInternalResponse(job);
    }
}
