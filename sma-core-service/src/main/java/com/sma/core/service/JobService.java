package com.sma.core.service;

import com.sma.core.dto.request.job.JobSearchRequest;
import com.sma.core.dto.response.job.BaseJobResponse;
import com.sma.core.dto.response.job.JobResponse;
import com.sma.core.dto.response.job.PublicJobResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobService {

    Page<BaseJobResponse> getAllJob(JobSearchRequest request);
    Page<JobResponse> getAllJobAsAdmin(JobSearchRequest request);
    Page<JobResponse> getAllJobAsRecruiter(JobSearchRequest request);
    Page<PublicJobResponse> getAllMySavedJob();
    PublicJobResponse getJobById(Integer id);

}
