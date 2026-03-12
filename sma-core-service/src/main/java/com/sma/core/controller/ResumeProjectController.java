package com.sma.core.controller;

import com.sma.core.dto.request.resume.UpdateResumeProjectRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.resume.ResumeProjectResponse;
import com.sma.core.service.ResumeProjectService;
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
@RequestMapping("/v1/resumes/{resumeId}/projects")
@RequiredArgsConstructor
public class ResumeProjectController {
    final ResumeProjectService resumeProjectService;

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Create project")
    public ApiResponse<ResumeProjectResponse> createProject(
            @PathVariable Integer resumeId,
            @RequestBody UpdateResumeProjectRequest request
    ) {
        return ApiResponse.<ResumeProjectResponse>builder()
                .message("Create resume project successfully")
                .data(resumeProjectService.create(resumeId, request))
                .build();
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Update project")
    public ApiResponse<ResumeProjectResponse> updateProject(
            @PathVariable Integer resumeId,
            @PathVariable Integer projectId,
            @RequestBody UpdateResumeProjectRequest request
    ) {
        return ApiResponse.<ResumeProjectResponse>builder()
                .message("Update resume project successfully")
                .data(resumeProjectService.update(resumeId, projectId, request))
                .build();
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Delete project")
    public ApiResponse<Void> deleteProject(
            @PathVariable Integer resumeId,
            @PathVariable Integer projectId
    ) {
        resumeProjectService.delete(resumeId, projectId);
        return ApiResponse.<Void>builder()
                .message("Delete resume project successfully")
                .build();
    }
}
