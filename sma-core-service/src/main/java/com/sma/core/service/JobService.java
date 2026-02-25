package com.sma.core.service;

import com.sma.core.dto.request.job.*;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.job.BaseJobResponse;
import com.sma.core.dto.response.job.JobDetailResponse;

public interface JobService {

    PagingResponse<BaseJobResponse> getAllJob(JobFilterRequest request);

    Boolean markJob(Integer jobId);
    PagingResponse<BaseJobResponse> getAllMyFavoriteJob(Integer page, Integer size);
    PagingResponse<BaseJobResponse> getAllMyAppliedJob(Integer page, Integer size);

    JobDetailResponse getJobById(Integer id);

    void closeExpiredJob();

    JobDetailResponse saveJob(DraftJobRequest request);
    JobDetailResponse saveExistingJob(Integer id, DraftJobRequest request);
    JobDetailResponse publishExistingJob(Integer id, PublishJobRequest request);
    JobDetailResponse updateJobStatus(Integer id, UpdateJobStatusRequest request);
    PagingResponse<BaseJobResponse> getJobsByCurrentCompany(JobFilterRequest request);
    JobDetailResponse updateAiSettings(Integer jobId, JobAiSettingsRequest request);
}
