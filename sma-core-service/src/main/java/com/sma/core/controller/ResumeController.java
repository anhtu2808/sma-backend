package com.sma.core.controller;

import com.sma.core.dto.request.resume.UploadResumeRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.resume.ResumeResponse;
import com.sma.core.service.ResumeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/v1/resumes")
@RequiredArgsConstructor
public class ResumeController {
    final ResumeService resumeService;

    @PostMapping({"", "/upload"})
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<ResumeResponse> uploadResume(@RequestBody UploadResumeRequest request) {
        return ApiResponse.<ResumeResponse>builder()
                .message("Upload resume successfully")
                .data(resumeService.uploadResume(request))
                .build();
    }

    @PostMapping("/{resumeId}/re-parse")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<ResumeResponse> reparseResume(@PathVariable Integer resumeId) {
        return ApiResponse.<ResumeResponse>builder()
                .message("Re-parse resume enqueued successfully")
                .data(resumeService.reparseResume(resumeId))
                .build();
    }

    @GetMapping("/{resumeId}/status")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<String> getResumeStatus(@PathVariable Integer resumeId) {
        return ApiResponse.<String>builder()
                .message("Get resume parsing status successfully")
                .data(resumeService.getResumeStatus(resumeId))
                .build();
    }
}
