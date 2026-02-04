package com.sma.core.service.impl;

import com.sma.core.dto.request.job.JobSearchRequest;
import com.sma.core.dto.response.job.BaseJobResponse;
import com.sma.core.dto.response.job.JobInternalResponse;
import com.sma.core.dto.response.job.JobDetailResponse;
import com.sma.core.entity.Job;
import com.sma.core.entity.Recruiter;
import com.sma.core.enums.JobStatus;
import com.sma.core.enums.Role;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.job.JobMapper;
import com.sma.core.repository.JobRepository;
import com.sma.core.repository.RecruiterRepository;
import com.sma.core.repository.spec.JobSpecification;
import com.sma.core.service.JobService;
import com.sma.core.utils.JwtTokenProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    final JobRepository jobRepository;
    final JobMapper jobMapper;
    final RecruiterRepository recruiterRepository;

    @Override
    public JobDetailResponse getJobById(Integer id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));
        // handle restrict candidate access to SUSPENDED, DRAFT, PENDING REVIEW job
        if (JwtTokenProvider.getCurrentRole().equals(Role.CANDIDATE)) {
            EnumSet<JobStatus> allowedStatus = EnumSet.of(JobStatus.APPROVED, JobStatus.CLOSED);
            if(!allowedStatus.contains(job.getStatus()))
                throw new AppException(ErrorCode.JOB_NOT_AVAILABLE);
        }
        return jobMapper.toJobDetailResponse(job);
    }

    /**
     * Get all job on job board page
     */
    @Override
    public Page<BaseJobResponse> getAllJob(JobSearchRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        EnumSet<JobStatus> allowedStatus = EnumSet.of(JobStatus.APPROVED);
        return jobRepository.findAll(JobSpecification.withFilter(request, allowedStatus, null), pageable)
                .map(jobMapper::toBaseJobResponse);
    }

    /**
     * Get all job on admin dashboard page
     */
    @Override
    public Page<JobInternalResponse> getAllJobAsAdmin(JobSearchRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        EnumSet<JobStatus> allowedStatus = EnumSet.noneOf(JobStatus.class);
        return jobRepository.findAll(JobSpecification.withFilter(request, allowedStatus, null), pageable)
                .map(jobMapper::toJobInternalResponse);
    }

    /**
     * Get all job on recruiter dashboard page
     */
    @Override
    public Page<JobInternalResponse> getAllJobAsRecruiter(JobSearchRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        EnumSet<JobStatus> allowedStatus = EnumSet.noneOf(JobStatus.class);
        Recruiter recruiter = recruiterRepository.getReferenceById(JwtTokenProvider.getCurrentActorId());
        return jobRepository.findAll(JobSpecification.withFilter(request, allowedStatus, recruiter.getCompany().getId()), pageable)
                .map(jobMapper::toJobInternalResponse);
    }

    /**
     * Get all saved job of current user on candidate dashboard
     */
    @Override
    public Page<BaseJobResponse> getAllMySavedJob() {
        return null;
    }

}
