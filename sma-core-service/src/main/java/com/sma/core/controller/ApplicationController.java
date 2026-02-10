package com.sma.core.controller;

import com.sma.core.dto.request.application.ApplicationRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.application.ApplicationResponse;
import com.sma.core.service.ApplicationService;
import com.sma.core.utils.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/applications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationController {

    ApplicationService applicationService;

    @PostMapping()
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<ApplicationResponse> applyToJob(@RequestBody @Valid ApplicationRequest request) {

        // Sử dụng code get ID bạn đã có
        Integer currentCandidateId = JwtTokenProvider.getCurrentCandidateId();

        return ApiResponse.<ApplicationResponse>builder()
                .message("Your application has been submitted successfully.")
                .data(applicationService.applyToJob(request, currentCandidateId))
                .build();
    }
}
