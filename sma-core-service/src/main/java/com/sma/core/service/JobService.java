package com.sma.core.service;

import com.sma.core.dto.request.job.JobSearchRequest;
import com.sma.core.dto.response.job.JobResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobService {

    Page<JobResponse> getAllJobAsCandidate(JobSearchRequest request);
    JobResponse getJobById(Integer id);

}
