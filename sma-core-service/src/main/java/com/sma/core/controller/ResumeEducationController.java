package com.sma.core.controller;

import com.sma.core.dto.request.resume.UpdateResumeEducationRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.resume.ResumeEducationDetailResponse;
import com.sma.core.service.ResumeEducationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/v1/resumes/{resumeId}/educations")
@RequiredArgsConstructor
public class ResumeEducationController {
    final ResumeEducationService resumeEducationService;

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Create education")
    public ApiResponse<ResumeEducationDetailResponse> createEducation(
            @PathVariable Integer resumeId,
            @RequestBody UpdateResumeEducationRequest request
    ) {
        return ApiResponse.<ResumeEducationDetailResponse>builder()
                .message("Create resume education successfully")
                .data(resumeEducationService.create(resumeId, request))
                .build();
    }

    @PutMapping("/{educationId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Update education")
    public ApiResponse<ResumeEducationDetailResponse> updateEducation(
            @PathVariable Integer resumeId,
            @PathVariable Integer educationId,
            @RequestBody UpdateResumeEducationRequest request
    ) {
        return ApiResponse.<ResumeEducationDetailResponse>builder()
                .message("Update resume education successfully")
                .data(resumeEducationService.update(resumeId, educationId, request))
                .build();
    }

    @DeleteMapping("/{educationId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Delete education")
    public ApiResponse<Void> deleteEducation(
            @PathVariable Integer resumeId,
            @PathVariable Integer educationId
    ) {
        resumeEducationService.delete(resumeId, educationId);
        return ApiResponse.<Void>builder()
                .message("Delete resume education successfully")
                .build();
    }
}
