package com.sma.core.service.impl;

import com.sma.core.dto.request.application.AnswerRequest;
import com.sma.core.dto.request.application.ApplicationFilter;
import com.sma.core.dto.request.application.ApplicationRequest;
import com.sma.core.dto.response.application.ApplicationListResponse;
import com.sma.core.dto.response.application.ApplicationResponse;
import com.sma.core.entity.*;
import com.sma.core.enums.ApplicationStatus;
import com.sma.core.enums.ResumeParseStatus;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.ApplicationMapper;
import com.sma.core.repository.*;
import com.sma.core.service.ApplicationService;
import com.sma.core.utils.JwtTokenProvider;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.ListJoin;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import jakarta.persistence.criteria.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {
    ApplicationRepository applicationRepository;
    JobRepository jobRepository;
    ResumeRepository resumeRepository;
    CandidateRepository candidateRepository;
    ApplicationMapper applicationMapper;
    RecruiterRepository recruiterRepository;

    @Override
    @Transactional
    public ApplicationResponse applyToJob(ApplicationRequest request, Integer currentCandidateId) {
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));
        Resume resume = resumeRepository.findById(request.getResumeId())
                .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_EXISTED));
        if (Boolean.TRUE.equals(resume.getIsDeleted())) {
            throw new AppException(ErrorCode.RESUME_ALREADY_DELETED);
        }
        if (!resume.getCandidate().getId().equals(currentCandidateId)) {
            throw new AppException(ErrorCode.NOT_HAVE_PERMISSION);
        }
        if (resume.getParseStatus() == ResumeParseStatus.FAIL) {
            throw new AppException(ErrorCode.RESUME_PARSE_FAILED);
        }
        if (resume.getParseStatus() == ResumeParseStatus.WAITING) {
            throw new AppException(ErrorCode.RESUME_STILL_PARSING);
        }
        Candidate candidate = candidateRepository.findById(currentCandidateId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        validateApplicationRules(candidate.getId(), job.getId());
        validateJobAnswers(job, request.getAnswers());
        long currentAttempts = applicationRepository.countByCandidateIdAndJobId(candidate.getId(), job.getId());
        int newAttemptValue = (int) currentAttempts + 1;
        Application application = Application.builder()
                .status(ApplicationStatus.APPLIED)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .coverLetter(request.getCoverLetter())
                .appliedAt(LocalDateTime.now())
                .attempt(newAttemptValue)
                .job(job)
                .resume(resume)
                .candidate(candidate)
                .build();

        if (request.getAnswers() != null && !request.getAnswers().isEmpty()) {
            Set<JobAnswer> answers = request.getAnswers().stream().map(ansReq -> {
                JobQuestion question = job.getQuestions().stream()
                        .filter(q -> q.getId().equals(ansReq.getQuestionId()))
                        .findFirst()
                        .orElseThrow(() -> new AppException(ErrorCode.JOB_QUESTION_NOT_EXIST));

                return JobAnswer.builder()
                        .application(application)
                        .jobQuestion(question)
                        .answer(ansReq.getAnswerContent())
                        .name(question.getQuestion())
                        .build();
            }).collect(Collectors.toSet());

            application.setAnswers(answers);
        }

        Application savedApplication = applicationRepository.save(application);
        log.info("Candidate {} applied to Job {} successfully (Attempt {})", currentCandidateId, job.getId(), newAttemptValue);

        return applicationMapper.toResponse(savedApplication);
    }

    private void validateApplicationRules(Integer candidateId, Integer jobId) {
        List<ApplicationStatus> rejectStatuses = List.of(
                ApplicationStatus.AUTO_REJECTED,
                ApplicationStatus.NOT_SUITABLE
        );
        if (applicationRepository.hasBeenRejected(candidateId, jobId, rejectStatuses)) {
            throw new AppException(ErrorCode.ALREADY_REJECTED_FOR_THIS_JOB);
        }

        applicationRepository.findFirstByCandidateIdAndJobIdOrderByAppliedAtDesc(candidateId, jobId)
                .ifPresent(lastApp -> {
                    long attemptCount = applicationRepository.countByCandidateIdAndJobId(candidateId, jobId);
                    if (attemptCount >= 2) {
                        throw new AppException(ErrorCode.MAX_APPLY_ATTEMPTS_REACHED);
                    }

                    if (lastApp.getStatus() != ApplicationStatus.APPLIED) {
                        throw new AppException(ErrorCode.CANNOT_REAPPLY_AFTER_PROCESSING);
                    }
                });
    }

    private void validateJobAnswers(Job job, List<AnswerRequest> answerRequests) {
        Set<Integer> requiredQuestionIds = job.getQuestions().stream()
                .filter(q -> Boolean.TRUE.equals(q.getIsRequired()) && !Boolean.TRUE.equals(q.getDeleted()))
                .map(JobQuestion::getId)
                .collect(Collectors.toSet());

        Set<Integer> submittedAnswerIds = (answerRequests == null) ? Set.of() :
                answerRequests.stream()
                        .filter(ans -> ans.getAnswerContent() != null && !ans.getAnswerContent().trim().isEmpty())
                        .map(AnswerRequest::getQuestionId)
                        .collect(Collectors.toSet());
        for (Integer requiredId : requiredQuestionIds) {
            if (!submittedAnswerIds.contains(requiredId)) {
                throw new AppException(ErrorCode.REQUIRED_QUESTION_NOT_ANSWERED);
            }
        }
    }


    @Override
    public Page<ApplicationListResponse> getApplicationsForRecruiter(ApplicationFilter filter) {

        Integer currentRecruiterId = JwtTokenProvider.getCurrentRecruiterId();

        Recruiter currentRecruiter = recruiterRepository.findById(currentRecruiterId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Integer currentCompanyId = currentRecruiter.getCompany().getId();
        Job job = jobRepository.findById(filter.getJobId())
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));

        if (!job.getCompany().getId().equals(currentCompanyId)) {
            log.warn("Security Alert: Recruiter {} tried to access applications for Job {} belonging to another company",
                    currentRecruiterId, filter.getJobId());
            throw new AppException(ErrorCode.NOT_HAVE_PERMISSION);
        }
        Specification<Application> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Application, Resume> resumeJoin = root.join("resume", JoinType.LEFT);

            predicates.add(cb.equal(root.get("job").get("id"), filter.getJobId()));

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            if (StringUtils.hasText(filter.getLocation())) {
                predicates.add(cb.like(cb.lower(resumeJoin.get("addressInResume")),
                        "%" + filter.getLocation().toLowerCase() + "%"));
            }

            if (filter.getSkills() != null && !filter.getSkills().isEmpty()) {
                Join<Resume, ResumeSkillGroup> skillJoin = resumeJoin.join("skillGroups");
                predicates.add(skillJoin.get("name").in(filter.getSkills()));
            }

            if (filter.getMatchLevel() != null) {
                ListJoin<Resume, ResumeEvaluation> evalJoin = resumeJoin.joinList("evaluations");
                predicates.add(cb.equal(evalJoin.get("job").get("id"), filter.getJobId()));
                predicates.add(cb.equal(evalJoin.get("matchLevel"), filter.getMatchLevel()));
            }
            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), Sort.by("appliedAt").descending());
        return applicationRepository.findAll(spec, pageable).map(this::convertToResponse);
    }

    private ApplicationListResponse convertToResponse(Application app) {
        Resume resume = app.getResume();
        double totalYears = 0;

        if (resume.getExperiences() != null) {
            totalYears = resume.getExperiences().stream()
                    .filter(exp -> exp.getStartDate() != null && exp.getEndDate() != null)
                    .mapToDouble(exp -> {
                        long days = java.time.temporal.ChronoUnit.DAYS.between(exp.getStartDate(), exp.getEndDate());
                        return days / 365.0;
                    })
                    .sum();
        }

        ResumeEvaluation evaluation = resume.getEvaluations().stream()
                .filter(e -> e.getJob().getId().equals(app.getJob().getId()))
                .findFirst()
                .orElse(null);

        return ApplicationListResponse.builder()
                .applicationId(app.getId())
                .candidateName(app.getFullName())
                .candidateEmail(app.getEmail())
                .status(app.getStatus())
                .appliedAt(app.getAppliedAt())
                .resumeUrl(resume.getResumeUrl())
                .location(resume.getAddressInResume())
                .language(resume.getLanguage())
                .totalExperienceYears(Math.round(totalYears * 10.0) / 10.0)
                .topSkills(resume.getSkillGroups().stream()
                        .flatMap(group -> group.getSkills().stream())
                        .map(resumeSkill -> resumeSkill.getSkill().getName())
                        .distinct()
                        .limit(5)
                        .collect(Collectors.toList()))
                .aiScore(evaluation != null ? evaluation.getAiOverallScore() : null)
                .matchLevel(evaluation != null ? evaluation.getMatchLevel() : null)
                .aiSummary(evaluation != null ? evaluation.getSummary() : null)
                .build();
    }
}
