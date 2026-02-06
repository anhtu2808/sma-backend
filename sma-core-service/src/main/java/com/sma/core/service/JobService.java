package com.sma.core.service;

import com.sma.core.dto.request.job.DraftJobRequest;
import com.sma.core.dto.request.job.PublishJobRequest;
import com.sma.core.dto.request.job.JobFilterRequest;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.job.BaseJobResponse;
import com.sma.core.dto.response.job.JobDetailResponse;

public interface JobService {

    PagingResponse<BaseJobResponse> getAllJob(JobFilterRequest request);

    PagingResponse<BaseJobResponse> getAllMySavedJob();

    JobDetailResponse getJobById(Integer id);

    void closeExpiredJob();

    JobDetailResponse publishJob(PublishJobRequest request);

    JobDetailResponse draftJob(DraftJobRequest request);

    JobDetailResponse publishExistingJob(Integer id, PublishJobRequest request);

}
