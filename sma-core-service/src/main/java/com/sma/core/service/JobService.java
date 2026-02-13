package com.sma.core.service;

import com.sma.core.dto.request.job.DraftJobRequest;
import com.sma.core.dto.request.job.PublishJobRequest;
import com.sma.core.dto.request.job.JobFilterRequest;
import com.sma.core.dto.request.job.UpdateJobStatusRequest;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.job.BaseJobResponse;
import com.sma.core.dto.response.job.JobDetailResponse;

public interface JobService {

    PagingResponse<BaseJobResponse> getAllJob(JobFilterRequest request);

    Boolean markJob(Integer jobId);
    PagingResponse<BaseJobResponse> getAllMyFavoriteJob(Integer page, Integer size);

    JobDetailResponse getJobById(Integer id);

    void closeExpiredJob();

    JobDetailResponse saveJob(DraftJobRequest request);
    JobDetailResponse saveExistingJob(Integer id, DraftJobRequest request);
    JobDetailResponse publishExistingJob(Integer id, PublishJobRequest request);
    JobDetailResponse updateJobStatus(Integer id, UpdateJobStatusRequest request);
    PagingResponse<BaseJobResponse> getJobsByCurrentCompany(JobFilterRequest request);
}
