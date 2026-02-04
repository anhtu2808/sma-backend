package com.sma.core.service.impl;

import com.sma.core.dto.request.job.JobFilterRequest;
import com.sma.core.dto.response.job.BaseJobResponse;
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
import com.sma.core.specification.JobSpecification;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

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
        Role role = JwtTokenProvider.getCurrentRole();
        if (role == null || role.equals(Role.CANDIDATE)) {
            EnumSet<JobStatus> allowedStatus = EnumSet.of(JobStatus.PUBLISHED, JobStatus.CLOSED);
            if(!allowedStatus.contains(job.getStatus()))
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

    /**
     * Get all job on job board page
     */
    @Override
    public Page<BaseJobResponse> getAllJob(JobFilterRequest request) {
        Role role = JwtTokenProvider.getCurrentRole();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        EnumSet<JobStatus> allowedStatus = null;
        LocalDateTime date = null;
        if (role == null || role.equals(Role.CANDIDATE)) {
            allowedStatus = EnumSet.of(JobStatus.PUBLISHED);
            date = LocalDateTime.now();
        } else if (role.equals(Role.RECRUITER) || role.equals(Role.ADMIN)) {
            if (!request.getStatuses().isEmpty())
                allowedStatus = request.getStatuses();
            else
                allowedStatus = EnumSet.noneOf(JobStatus.class);
            if (role.equals(Role.RECRUITER)) {
                Recruiter recruiter = recruiterRepository.getReferenceById(JwtTokenProvider.getCurrentActorId());
                request.setCompanyId(recruiter.getCompany().getId());
            }
        }
        return jobRepository.findAll(JobSpecification.withFilter(request, allowedStatus, date), pageable)
                .map(jobMapper::toBaseJobResponse);
    }

    /**
     * Get all saved job of current user on candidate dashboard
     */
    @Override
    public Page<BaseJobResponse> getAllMySavedJob() {
        return null;
    }

}
