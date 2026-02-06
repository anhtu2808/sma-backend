package com.sma.core.controller;

import com.sma.core.dto.request.job.DraftJobRequest;
import com.sma.core.dto.request.job.PublishJobRequest;
import com.sma.core.dto.request.job.JobFilterRequest;
import com.sma.core.dto.request.job.UpdateJobStatusRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.job.BaseJobResponse;
import com.sma.core.dto.response.job.JobDetailResponse;
import com.sma.core.service.JobService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/v1/jobs")
@RequiredArgsConstructor
public class JobController {
    final JobService jobService;

    @GetMapping
    public ApiResponse<PagingResponse<BaseJobResponse>> getAllJob(@ParameterObject JobFilterRequest request) {
        return ApiResponse.<PagingResponse<BaseJobResponse>>builder()
                .message("Get all job successfully")
                .data(jobService.getAllJob(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<JobDetailResponse> getJobById(@PathVariable Integer id) {
        return ApiResponse.<JobDetailResponse>builder()
                .message("Get job by id successfully")
                .data(jobService.getJobById(id))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<JobDetailResponse> draftJob(@RequestBody DraftJobRequest request) {
        return ApiResponse.<JobDetailResponse>builder()
                .message("Draft job successfully")
                .data(jobService.draftJob(request))
                .build();
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<JobDetailResponse> publishExistingJob(@RequestBody PublishJobRequest request,
            @PathVariable Integer id) {
        return ApiResponse.<JobDetailResponse>builder()
                .message("Publish job successfully")
                .data(jobService.publishExistingJob(id, request))
                .build();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ApiResponse<JobDetailResponse> updateJobStatus(@RequestBody UpdateJobStatusRequest request,
                                                          @PathVariable Integer id) {
        return ApiResponse.<JobDetailResponse>builder()
                .message("Update job status successfully")
                .data(jobService.updateJobStatus(id, request))
                .build();
    }
}
