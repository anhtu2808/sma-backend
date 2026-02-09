package com.sma.core.service;

import com.sma.core.dto.request.question.UpsertQuestionRequest;
import com.sma.core.dto.request.question.JobQuestionFilterRequest;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.question.JobQuestionResponse;

import java.util.Set;

public interface JobQuestionService {
    JobQuestionResponse create(Integer jobId, UpsertQuestionRequest request);
    JobQuestionResponse update(Integer id, UpsertQuestionRequest request);
    void delete(Integer id);
    JobQuestionResponse getById(Integer id);
    PagingResponse<JobQuestionResponse> getAll(JobQuestionFilterRequest filter);
    Set<JobQuestionResponse> getByJobId(Integer jobId);
}
