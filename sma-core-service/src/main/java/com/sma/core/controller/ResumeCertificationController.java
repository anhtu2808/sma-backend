package com.sma.core.controller;

import com.sma.core.dto.request.resume.UpdateResumeCertificationRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.resume.ResumeCertificationDetailResponse;
import com.sma.core.service.ResumeCertificationService;
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
@RequestMapping("/v1/resumes/{resumeId}/certifications")
@RequiredArgsConstructor
public class ResumeCertificationController {
    final ResumeCertificationService resumeCertificationService;

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Create certification")
    public ApiResponse<ResumeCertificationDetailResponse> createCertification(
            @PathVariable Integer resumeId,
            @RequestBody UpdateResumeCertificationRequest request
    ) {
        return ApiResponse.<ResumeCertificationDetailResponse>builder()
                .message("Create resume certification successfully")
                .data(resumeCertificationService.create(resumeId, request))
                .build();
    }

    @PutMapping("/{certificationId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Update certification")
    public ApiResponse<ResumeCertificationDetailResponse> updateCertification(
            @PathVariable Integer resumeId,
            @PathVariable Integer certificationId,
            @RequestBody UpdateResumeCertificationRequest request
    ) {
        return ApiResponse.<ResumeCertificationDetailResponse>builder()
                .message("Update resume certification successfully")
                .data(resumeCertificationService.update(resumeId, certificationId, request))
                .build();
    }

    @DeleteMapping("/{certificationId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Delete certification")
    public ApiResponse<Void> deleteCertification(
            @PathVariable Integer resumeId,
            @PathVariable Integer certificationId
    ) {
        resumeCertificationService.delete(resumeId, certificationId);
        return ApiResponse.<Void>builder()
                .message("Delete resume certification successfully")
                .build();
    }
}
