package com.sma.core.controller;

import com.sma.core.dto.request.job.JobFilterRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.job.BaseJobResponse;
import com.sma.core.dto.response.job.JobDetailResponse;
import com.sma.core.service.JobService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/v1/jobs")
@RequiredArgsConstructor
public class JobController {
    final JobService jobService;

    @GetMapping
    public ApiResponse<Page<BaseJobResponse>> getAllJob(@ParameterObject JobFilterRequest request)
    {
        return ApiResponse.<Page<BaseJobResponse>>builder()
                .message("Get all job successfully")
                .data(jobService.getAllJob(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<JobDetailResponse> getJobById(@PathVariable Integer id)
    {
        return ApiResponse.<JobDetailResponse>builder()
                .message("Get job by id successfully")
                .data(jobService.getJobById(id))
                .build();
    }
}
