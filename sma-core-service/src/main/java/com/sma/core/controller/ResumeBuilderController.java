package com.sma.core.controller;

import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.resume.ResumeResponse;
import com.sma.core.enums.ResumeType;
import com.sma.core.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/v1/resumes")
@RequiredArgsConstructor
public class ResumeBuilderController {
    final ResumeService resumeService;

    @PostMapping("/builder")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(
            summary = "Tạo mới 1 Resume Builder trống"
    )
    public ApiResponse<ResumeResponse> createResumeBuilder() {
        return ApiResponse.<ResumeResponse>builder()
                .message("Create resume builder")
                .data(resumeService.createResumeBuilder())
                .build();
    }

    @PostMapping("/{resumeId}/builder")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(
            summary = "Tạo Resume Builder từ resume có sẵn",
            description = "Clone resume PDF thành dạng TEMPLATE để chỉnh sửa trong Resume Builder"
    )
    public ApiResponse<ResumeResponse> cloneResumeBuilder(@PathVariable Integer resumeId) {
        return ApiResponse.<ResumeResponse>builder()
                .message("Create resume builder")
                .data(resumeService.cloneResume(resumeId, ResumeType.TEMPLATE))
                .build();
    }
}
