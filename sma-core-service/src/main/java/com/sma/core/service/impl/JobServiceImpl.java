package com.sma.core.service.impl;

import com.sma.core.dto.request.job.JobSearchRequest;
import com.sma.core.dto.response.job.JobResponse;
import com.sma.core.entity.Job;
import com.sma.core.enums.JobStatus;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.JobMapper;
import com.sma.core.repository.JobRepository;
import com.sma.core.service.JobService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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

    @Override
    public Page<JobResponse> getAllJobAsCandidate(JobSearchRequest request) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(request.getPage(), request.getSize());
        return jobRepository.findAll(com.sma.core.repository.spec.JobSpecification.withFilter(request), pageable)
                .map(jobMapper::toOverallJobResponse);
    }

    @Override
    public JobResponse getJobByIdAsCandidate(Integer id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));
        EnumSet<JobStatus> allowedStatus = EnumSet.of(JobStatus.APPROVED, JobStatus.CLOSED);
        if(!allowedStatus.contains(job.getStatus()))
            throw new AppException(ErrorCode.JOB_NOT_AVAILABLE);

        return jobMapper.toJobResponse(job);
    }
}
