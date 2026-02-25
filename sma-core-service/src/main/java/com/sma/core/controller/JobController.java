package com.sma.core.controller;

import com.sma.core.dto.request.job.*;
import com.sma.core.dto.request.question.UpsertQuestionRequest;
import com.sma.core.dto.request.question.JobQuestionFilterRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.job.BaseJobResponse;
import com.sma.core.dto.response.job.JobDetailResponse;
import com.sma.core.dto.response.question.JobQuestionResponse;
import com.sma.core.service.JobQuestionService;
import com.sma.core.service.JobService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import static org.springframework.security.authorization.AuthorityReactiveAuthorizationManager.hasAnyRole;

@RestController
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/v1/jobs")
@RequiredArgsConstructor
public class JobController {
    final JobService jobService;
    final JobQuestionService jobQuestionService;

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
    public ApiResponse<JobDetailResponse> saveJob(@RequestBody DraftJobRequest request) {
        return ApiResponse.<JobDetailResponse>builder()
                .message("Save job successfully")
                .data(jobService.saveJob(request))
                .build();
    }

    @PutMapping("/{id}/save")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<JobDetailResponse> saveExistingJob(@RequestBody DraftJobRequest request, @PathVariable Integer id) {
        return ApiResponse.<JobDetailResponse>builder()
                .message("Save job successfully")
                .data(jobService.saveExistingJob(id ,request))
                .build();
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<JobDetailResponse> publishExistingJob(@RequestBody @Valid PublishJobRequest request,
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

    @PutMapping("/{id}/threshold")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<JobDetailResponse> updateJobThreshold(@RequestBody UpdateJobStatusRequest request,
                                                          @PathVariable Integer id) {
        return ApiResponse.<JobDetailResponse>builder()
                .message("Update job status successfully")
                .data(jobService.updateJobStatus(id, request))
                .build();
    }

    @PostMapping("/{jobId}/job-questions")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<JobQuestionResponse> create(
            @PathVariable Integer jobId,
            @RequestBody @Valid UpsertQuestionRequest request) {
        return ApiResponse.<JobQuestionResponse>builder()
                .data(jobQuestionService.create(jobId, request))
                .message("Question created successfully")
                .build();
    }

    @GetMapping("/{jobId}/job-questions")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN', 'CANDIDATE')")
    public ApiResponse<Set<JobQuestionResponse>> getByJobId(
            @PathVariable Integer jobId) {
        return ApiResponse.<Set<JobQuestionResponse>>builder()
                .message("Get job questions by job id successfully")
                .data(jobQuestionService.getByJobId(jobId))
                .build();
    }

    @PostMapping("/{jobId}/mark")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<Boolean> markJob(
            @PathVariable Integer jobId) {
        Boolean isMarked = jobService.markJob(jobId);
        return ApiResponse.<Boolean>builder()
                .message(isMarked ? "Mark job successfully" : "Unmark job successfully")
                .build();
    }

    @GetMapping("/marked")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<PagingResponse<BaseJobResponse>> getMarkedJob(
            @RequestParam Integer page, @RequestParam Integer size) {
        return ApiResponse.<PagingResponse<BaseJobResponse>>builder()
                .message("Get my marked job successfully")
                .data(jobService.getAllMyFavoriteJob(page, size))
                .build();
    }

    @GetMapping("/applied")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<PagingResponse<BaseJobResponse>> getAppliedJob(
            @RequestParam Integer page, @RequestParam Integer size) {
        return ApiResponse.<PagingResponse<BaseJobResponse>>builder()
                .message("Get my applied job successfully")
                .data(jobService.getAllMyAppliedJob(page, size))
                .build();
    }

    @GetMapping("/my-jobs")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ApiResponse<PagingResponse<BaseJobResponse>> getMyCompanyJobs(@ParameterObject JobFilterRequest request) {
        return ApiResponse.<PagingResponse<BaseJobResponse>>builder()
                .message("Get company jobs successfully")
                .data(jobService.getJobsByCurrentCompany(request))
                .build();
    }

    @PatchMapping("/{id}/ai-settings")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ApiResponse<JobDetailResponse> updateAiSettings(
            @PathVariable Integer id,
            @RequestBody @Valid JobAiSettingsRequest request) {

        return ApiResponse.<JobDetailResponse>builder()
                .message("Update AI scoring settings successfully")
                .data(jobService.updateAiSettings(id, request))
                .build();
    }

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
    public ApiResponse<JobDetailResponse> updateSampleJob(
            @PathVariable Integer id,
            @RequestBody @Valid AdminJobSampleRequest request) {
        return ApiResponse.<JobDetailResponse>builder()
                .message("Update sample job successfully")
                .data(jobService.updateSampleJob(id, request))
                .build();
    }

    @DeleteMapping("/samples/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteSampleJob(@PathVariable Integer id) {
        jobService.deleteSampleJob(id);
        return ApiResponse.<Void>builder()
                .message("Delete sample job successfully")
                .build();
    }

}
