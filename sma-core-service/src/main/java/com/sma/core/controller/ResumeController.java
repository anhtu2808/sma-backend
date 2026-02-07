package com.sma.core.controller;

import com.sma.core.dto.request.resume.UploadResumeRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.resume.ResumeDetailResponse;
import com.sma.core.dto.response.resume.ResumeResponse;
import com.sma.core.enums.ResumeType;
import com.sma.core.service.ResumeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/v1/resumes")
@RequiredArgsConstructor
public class ResumeController {
    final ResumeService resumeService;

    @GetMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<List<ResumeResponse>> getMyResumes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ResumeType type
    ) {
        return ApiResponse.<List<ResumeResponse>>builder()
                .message("Get candidate resumes successfully")
                .data(resumeService.getMyResumes(keyword, type))
                .build();
    }

    @PostMapping({"", "/upload"})
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<ResumeResponse> uploadResume(@RequestBody UploadResumeRequest request) {
        return ApiResponse.<ResumeResponse>builder()
                .message("Upload resume successfully")
                .data(resumeService.uploadResume(request))
                .build();
    }

    @GetMapping("/{resumeId}")
    public ApiResponse<ResumeDetailResponse> getResumeDetail(@PathVariable Integer resumeId) {
        return ApiResponse.<ResumeDetailResponse>builder()
                .message("Get resume detail successfully")
                .data(resumeService.getResumeDetail(resumeId))
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

    @GetMapping("/{resumeId}/parse-status")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<String> getResumeParseStatus(@PathVariable Integer resumeId) {
        return ApiResponse.<String>builder()
                .message("Get resume parse status successfully")
                .data(resumeService.getResumeParseStatus(resumeId))
                .build();
    }

    @DeleteMapping("/{resumeId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<Void> deleteResume(@PathVariable Integer resumeId) {
        resumeService.deleteResume(resumeId);
        return ApiResponse.<Void>builder()
                .message("Delete resume successfully")
                .build();
    }
}
