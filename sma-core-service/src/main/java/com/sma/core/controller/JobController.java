package com.sma.core.controller;

import com.sma.core.dto.request.job.JobSearchRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.job.BaseJobResponse;
import com.sma.core.dto.response.job.JobResponse;
import com.sma.core.dto.response.job.PublicJobResponse;
import com.sma.core.service.JobService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/v1/jobs")
@RequiredArgsConstructor
public class JobController {
    final JobService jobService;

    @GetMapping
    public ApiResponse<Page<BaseJobResponse>> getAllJob(@ParameterObject JobSearchRequest request)
    {
        return ApiResponse.<Page<BaseJobResponse>>builder()
                .message("Get all job successfully")
                .data(jobService.getAllJob(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<PublicJobResponse> getJobById(@PathVariable Integer id)
    {
        return ApiResponse.<PublicJobResponse>builder()
                .message("Get job by id successfully")
                .data(jobService.getJobById(id))
                .build();
    }
}
