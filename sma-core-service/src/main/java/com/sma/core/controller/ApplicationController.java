package com.sma.core.controller;

import com.sma.core.dto.request.application.ApplicationFilter;
import com.sma.core.dto.request.application.ApplicationRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.application.ApplicationDetailResponse;
import com.sma.core.dto.response.application.ApplicationListResponse;
import com.sma.core.dto.response.application.ApplicationResponse;
import com.sma.core.enums.ApplicationStatus;
import com.sma.core.service.ApplicationService;
import com.sma.core.utils.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/applications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationController {

    ApplicationService applicationService;

    @PostMapping()
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<ApplicationResponse> applyToJob(@RequestBody @Valid ApplicationRequest request) {
        Integer currentCandidateId = JwtTokenProvider.getCurrentCandidateId();
        return ApiResponse.<ApplicationResponse>builder()
                .message("Your application has been submitted successfully.")
                .data(applicationService.applyToJob(request, currentCandidateId))
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<Page<ApplicationListResponse>> getApplications(ApplicationFilter filter) {
        return ApiResponse.<Page<ApplicationListResponse>>builder()
                .data(applicationService.getApplicationsForRecruiter(filter))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECRUITER', 'CANDIDATE')")
    public ApiResponse<ApplicationDetailResponse> getDetail(@PathVariable Integer id) {
        return ApiResponse.<ApplicationDetailResponse>builder()
                .data(applicationService.getApplicationDetail(id))
                .build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<Void> updateStatus(
            @PathVariable Integer id,
            @RequestParam ApplicationStatus status,
            @RequestParam(required = false) String rejectReason) {

        applicationService.updateStatus(id, status, rejectReason);
        return ApiResponse.<Void>builder()
                .message("Update application status successfully.")
                .build();
    }
}
