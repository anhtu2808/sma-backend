package com.sma.core.controller.candidate;

import com.sma.core.dto.request.job.JobSearchRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.job.JobResponse;
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
@RequestMapping("/v1/candidate/jobs")
@RequiredArgsConstructor
public class CandidateJobController {
    final JobService jobService;

    @GetMapping
    public ApiResponse<Page<JobResponse>> getAllJobAsCandidate(@ParameterObject JobSearchRequest request)
    {
        return ApiResponse.<Page<JobResponse>>builder()
                .message("Get all job as candidate successfully")
                .data(jobService.getAllJobAsCandidate(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<JobResponse> getAllJobAsCandidate(@PathVariable Integer id)
    {
        return ApiResponse.<JobResponse>builder()
                .message("Get all job as candidate successfully")
                .data(jobService.getJobByIdAsCandidate(id))
                .build();
    }
}
