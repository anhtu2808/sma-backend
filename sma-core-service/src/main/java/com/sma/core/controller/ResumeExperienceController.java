package com.sma.core.controller;

import com.sma.core.dto.request.resume.UpdateResumeExperienceRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.resume.ResumeExperienceResponse;
import com.sma.core.service.ResumeExperienceService;
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
@RequestMapping("/v1/resumes/{resumeId}/experiences")
@RequiredArgsConstructor
public class ResumeExperienceController {
    final ResumeExperienceService resumeExperienceService;

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Create experience")
    public ApiResponse<ResumeExperienceResponse> createExperience(
            @PathVariable Integer resumeId,
            @RequestBody UpdateResumeExperienceRequest request
    ) {
        return ApiResponse.<ResumeExperienceResponse>builder()
                .message("Create resume experience successfully")
                .data(resumeExperienceService.create(resumeId, request))
                .build();
    }

    @PutMapping("/{experienceId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Update experience")
    public ApiResponse<ResumeExperienceResponse> updateExperience(
            @PathVariable Integer resumeId,
            @PathVariable Integer experienceId,
            @RequestBody UpdateResumeExperienceRequest request
    ) {
        return ApiResponse.<ResumeExperienceResponse>builder()
                .message("Update resume experience successfully")
                .data(resumeExperienceService.update(resumeId, experienceId, request))
                .build();
    }

    @DeleteMapping("/{experienceId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Delete experience")
    public ApiResponse<Void> deleteExperience(
            @PathVariable Integer resumeId,
            @PathVariable Integer experienceId
    ) {
        resumeExperienceService.delete(resumeId, experienceId);
        return ApiResponse.<Void>builder()
                .message("Delete resume experience successfully")
                .build();
    }
}
