package com.sma.core.service;

import com.sma.core.dto.request.job.DraftJobRequest;
import com.sma.core.dto.request.job.PublishJobRequest;
import com.sma.core.dto.request.job.JobFilterRequest;
import com.sma.core.dto.response.job.BaseJobResponse;
import com.sma.core.dto.response.job.JobDetailResponse;
import org.springframework.data.domain.Page;

public interface JobService {

    Page<BaseJobResponse> getAllJob(JobFilterRequest request);
    Page<BaseJobResponse> getAllMySavedJob();
    JobDetailResponse getJobById(Integer id);
    void closeExpiredJob();
    JobDetailResponse publishJob(PublishJobRequest request);
    JobDetailResponse draftJob(DraftJobRequest request);
    JobDetailResponse publishExistingJob(Integer id, PublishJobRequest request);

}
