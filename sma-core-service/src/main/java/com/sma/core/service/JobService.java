package com.sma.core.service;

import com.sma.core.dto.request.job.JobSearchRequest;
import com.sma.core.dto.response.job.BaseJobResponse;
import com.sma.core.dto.response.job.JobDetailResponse;
import org.springframework.data.domain.Page;

public interface JobService {

    Page<BaseJobResponse> getAllJob(JobSearchRequest request);
    Page<BaseJobResponse> getAllMySavedJob();
    JobDetailResponse getJobById(Integer id);
    void closeExpiredJob();

}
