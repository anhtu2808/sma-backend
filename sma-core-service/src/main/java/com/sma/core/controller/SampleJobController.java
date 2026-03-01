package com.sma.core.controller;

import com.sma.core.dto.request.job.AdminJobSampleRequest;
import com.sma.core.dto.request.job.JobFilterRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.job.BaseJobResponse;
import com.sma.core.dto.response.job.JobDetailResponse;
import com.sma.core.service.JobService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/v1/jobs")
@RequiredArgsConstructor
public class SampleJobController {
    final JobService jobService;

    @PostMapping("/samples")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<JobDetailResponse> createSampleJob(@RequestBody @Valid AdminJobSampleRequest request) {
        return ApiResponse.<JobDetailResponse>builder()
                .message("Create sample job successfully")
                .data(jobService.createSampleJob(request))
                .build();
    }

    @GetMapping("/samples")
    @PreAuthorize("hasAnyRole('ADMIN', 'CANDIDATE')")
    public ApiResponse<PagingResponse<BaseJobResponse>> getSampleJobs(JobFilterRequest filterRequest) {
        return ApiResponse.<PagingResponse<BaseJobResponse>>builder()
                .message("Get sample jobs successfully")
                .data(jobService.getSampleJobs(filterRequest))
                .build();
    }

    @PutMapping("/samples/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<JobDetailResponse> updateSampleJob(@PathVariable Integer id, @RequestBody @Valid AdminJobSampleRequest request) {
        return ApiResponse.<JobDetailResponse>builder()
                .message("Update sample job successfully")
                .data(jobService.updateSampleJob(id, request))
                .build();
    }

    @DeleteMapping("/samples/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteSampleJob(@PathVariable Integer id) {
        jobService.deleteSampleJob(id);
        return ApiResponse.<Void>builder().message("Delete sample job successfully").build();
    }

}
