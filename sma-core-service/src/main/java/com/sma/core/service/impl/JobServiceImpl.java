package com.sma.core.service.impl;

import com.sma.core.dto.request.job.JobSearchRequest;
import com.sma.core.dto.response.job.JobResponse;
import com.sma.core.repository.JobRepository;
import com.sma.core.service.JobService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    final JobRepository jobRepository;

    @Override
    public Page<JobResponse> getAllJobAsCandidate(JobSearchRequest request) {
        return null;
    }
}
