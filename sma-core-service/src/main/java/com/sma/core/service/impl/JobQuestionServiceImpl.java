package com.sma.core.service.impl;

import com.sma.core.dto.request.question.UpsertQuestionRequest;
import com.sma.core.dto.request.question.JobQuestionFilterRequest;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.question.JobQuestionResponse;
import com.sma.core.entity.Job;
import com.sma.core.entity.JobQuestion;
import com.sma.core.entity.Recruiter;
import com.sma.core.enums.Role;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.job.JobQuestionMapper;
import com.sma.core.repository.JobQuestionRepository;
import com.sma.core.repository.JobRepository;
import com.sma.core.repository.RecruiterRepository;
import com.sma.core.service.JobQuestionService;
import com.sma.core.specification.JobQuestionSpecification;
import com.sma.core.utils.JwtTokenProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class JobQuestionServiceImpl implements JobQuestionService {
    JobQuestionRepository jobQuestionRepository;
    JobRepository jobRepository;
    JobQuestionMapper jobQuestionMapper;
    RecruiterRepository recruiterRepository;

    @Override
    public JobQuestionResponse create(Integer jobId, UpsertQuestionRequest request) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));

        JobQuestion jobQuestion = jobQuestionMapper.toQuestion(request);
        jobQuestion.setJob(job);
        jobQuestion.setDeleted(false);

        return jobQuestionMapper.toJobQuestionResponse(jobQuestionRepository.save(jobQuestion));
    }

    @Override
    public JobQuestionResponse update(Integer id, UpsertQuestionRequest request) {
        JobQuestion jobQuestion = jobQuestionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_QUESTION_NOT_EXIST));
        isPermission(jobQuestion);
        jobQuestionMapper.updateQuestion(request, jobQuestion);
        return jobQuestionMapper.toJobQuestionResponse(jobQuestionRepository.save(jobQuestion));
    }

    @Override
    public void delete(Integer id) {
        JobQuestion jobQuestion = jobQuestionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_QUESTION_NOT_EXIST));
        isPermission(jobQuestion);
        jobQuestion.setDeleted(true);
        jobQuestionRepository.save(jobQuestion);
    }

    @Override
    @Transactional(readOnly = true)
    public JobQuestionResponse getById(Integer id) {
        JobQuestion jobQuestion = jobQuestionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_QUESTION_NOT_EXIST));
        isPermission(jobQuestion);
        return jobQuestionMapper.toJobQuestionResponse(jobQuestion);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<JobQuestionResponse> getAll(JobQuestionFilterRequest filter) {
        Role role = JwtTokenProvider.getCurrentRole();
        Integer companyId = null;
        if(role != null && role.equals(Role.RECRUITER)){
            Recruiter recruiter = recruiterRepository.findById(JwtTokenProvider.getCurrentRecruiterId())
                    .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED));
            companyId = recruiter.getCompany().getId();
        }
        Specification<JobQuestion> spec = JobQuestionSpecification.withFilter(filter, null, companyId);
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());
        Page<JobQuestion> questions = jobQuestionRepository.findAll(spec, pageable);
        return PagingResponse.fromPage(questions.map(jobQuestionMapper::toJobQuestionResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<JobQuestionResponse> getByJobId(Integer jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));
        Set<JobQuestion> questions;
        if (Objects.equals(JwtTokenProvider.getCurrentRole(), Role.CANDIDATE) || !isPermission(job))
            questions = jobQuestionRepository.findByJob_IdAndDeletedFalse(jobId);
        else
            questions = jobQuestionRepository.findByJob_Id(jobId);
        return questions.stream()
                .map(jobQuestionMapper::toJobQuestionResponse)
                .collect(Collectors.toCollection(HashSet::new));
    }

    void isPermission(JobQuestion jobQuestion) {
        if (Objects.equals(JwtTokenProvider.getCurrentRole(), Role.RECRUITER)) {
            Recruiter recruiter = recruiterRepository.findById(JwtTokenProvider.getCurrentRecruiterId())
                    .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED));
            if (!recruiter.getCompany().getId().equals(jobQuestion.getJob().getCompany().getId())) {
                throw new AppException(ErrorCode.NOT_HAVE_PERMISSION);
            }
        }
    }

    Boolean isPermission(Job job) {
        if (Objects.equals(JwtTokenProvider.getCurrentRole(), Role.RECRUITER)) {
            Recruiter recruiter = recruiterRepository.findById(JwtTokenProvider.getCurrentRecruiterId())
                    .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED));
            return recruiter.getCompany().getId().equals(job.getCompany().getId());
        }
        return true;
    }
}


