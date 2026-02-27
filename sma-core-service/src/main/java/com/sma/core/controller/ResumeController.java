package com.sma.core.controller;

import com.sma.core.dto.request.resume.ExperienceSkillRequest;
import com.sma.core.dto.request.resume.UpdateProjectSkillRequest;
import com.sma.core.dto.request.resume.UpdateResumeCertificationRequest;
import com.sma.core.dto.request.resume.UpdateResumeEducationRequest;
import com.sma.core.dto.request.resume.UpdateResumeExperienceDetailRequest;
import com.sma.core.dto.request.resume.UpdateResumeExperienceRequest;
import com.sma.core.dto.request.resume.UpdateResumeProjectRequest;
import com.sma.core.dto.request.resume.UpdateResumeRequest;
import com.sma.core.dto.request.resume.UpdateResumeSkillRequest;
import com.sma.core.dto.request.resume.UploadResumeRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.resume.ExperienceSkillResponse;
import com.sma.core.dto.response.resume.ProjectSkillResponse;
import com.sma.core.dto.response.resume.ResumeCertificationDetailResponse;
import com.sma.core.dto.response.resume.ResumeDetailResponse;
import com.sma.core.dto.response.resume.ResumeEducationDetailResponse;
import com.sma.core.dto.response.resume.ResumeExperienceDetailResponse;
import com.sma.core.dto.response.resume.ResumeExperienceResponse;
import com.sma.core.dto.response.resume.ResumeProjectResponse;
import com.sma.core.dto.response.resume.ResumeResponse;
import com.sma.core.dto.response.resume.ResumeSkillDetailResponse;
import com.sma.core.enums.ResumeType;
import com.sma.core.service.ExperienceSkillService;
import com.sma.core.service.ProjectSkillService;
import com.sma.core.service.ResumeCertificationService;
import com.sma.core.service.ResumeEducationService;
import com.sma.core.service.ResumeExperienceDetailService;
import com.sma.core.service.ResumeExperienceService;
import com.sma.core.service.ResumeProjectService;
import com.sma.core.service.ResumeService;
import com.sma.core.service.ResumeSkillService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    final ResumeSkillService resumeSkillService;
    final ResumeEducationService resumeEducationService;
    final ResumeExperienceService resumeExperienceService;
    final ResumeExperienceDetailService resumeExperienceDetailService;
    final ExperienceSkillService experienceSkillService;
    final ResumeProjectService resumeProjectService;
    final ProjectSkillService projectSkillService;
    final ResumeCertificationService resumeCertificationService;

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

    @PutMapping("/{resumeId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<ResumeResponse> updateResume(@PathVariable Integer resumeId, @RequestBody UpdateResumeRequest request) {
        return ApiResponse.<ResumeResponse>builder()
                .message("Update resume successfully")
                .data(resumeService.updateResume(resumeId, request))
                .build();
    }

    @PostMapping("/{resumeId}/skills")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<ResumeSkillDetailResponse> createSkill(
            @PathVariable Integer resumeId,
            @RequestBody UpdateResumeSkillRequest request
    ) {
        return ApiResponse.<ResumeSkillDetailResponse>builder()
                .message("Create resume skill successfully")
                .data(resumeSkillService.create(resumeId, request))
                .build();
    }

    @PutMapping("/{resumeId}/skills/{resumeSkillId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<ResumeSkillDetailResponse> updateSkill(
            @PathVariable Integer resumeId,
            @PathVariable Integer resumeSkillId,
            @RequestBody UpdateResumeSkillRequest request
    ) {
        return ApiResponse.<ResumeSkillDetailResponse>builder()
                .message("Update resume skill successfully")
                .data(resumeSkillService.update(resumeId, resumeSkillId, request))
                .build();
    }

    @DeleteMapping("/{resumeId}/skills/{resumeSkillId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<Void> deleteSkill(
            @PathVariable Integer resumeId,
            @PathVariable Integer resumeSkillId
    ) {
        resumeSkillService.delete(resumeId, resumeSkillId);
        return ApiResponse.<Void>builder()
                .message("Delete resume skill successfully")
                .build();
    }

    @PostMapping("/{resumeId}/educations")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<ResumeEducationDetailResponse> createEducation(
            @PathVariable Integer resumeId,
            @RequestBody UpdateResumeEducationRequest request
    ) {
        return ApiResponse.<ResumeEducationDetailResponse>builder()
                .message("Create resume education successfully")
                .data(resumeEducationService.create(resumeId, request))
                .build();
    }

    @PutMapping("/{resumeId}/educations/{educationId}")
    @PreAuthorize("hasRole('CANDIDATE')")
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

    @PostMapping("/{resumeId}/experiences")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<ResumeExperienceResponse> createExperience(
            @PathVariable Integer resumeId,
            @RequestBody UpdateResumeExperienceRequest request
    ) {
        return ApiResponse.<ResumeExperienceResponse>builder()
                .message("Create resume experience successfully")
                .data(resumeExperienceService.create(resumeId, request))
                .build();
    }

    @PutMapping("/{resumeId}/experiences/{experienceId}")
    @PreAuthorize("hasRole('CANDIDATE')")
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

    @PostMapping("/{resumeId}/experiences/{experienceId}/details")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<ResumeExperienceDetailResponse> createExperienceDetail(
            @PathVariable Integer resumeId,
            @PathVariable Integer experienceId,
            @RequestBody UpdateResumeExperienceDetailRequest request
    ) {
        return ApiResponse.<ResumeExperienceDetailResponse>builder()
                .message("Create resume experience detail successfully")
                .data(resumeExperienceDetailService.create(resumeId, experienceId, request))
                .build();
    }

    @PutMapping("/{resumeId}/experience-details/{experienceDetailId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<ResumeExperienceDetailResponse> updateExperienceDetail(
            @PathVariable Integer resumeId,
            @PathVariable Integer experienceDetailId,
            @RequestBody UpdateResumeExperienceDetailRequest request
    ) {
        return ApiResponse.<ResumeExperienceDetailResponse>builder()
                .message("Update resume experience detail successfully")
                .data(resumeExperienceDetailService.update(resumeId, experienceDetailId, request))
                .build();
    }

    @PostMapping("/{resumeId}/experience-details/{experienceDetailId}/skills")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<ExperienceSkillResponse> createExperienceSkill(
            @PathVariable Integer resumeId,
            @PathVariable Integer experienceDetailId,
            @RequestBody ExperienceSkillRequest request
    ) {
        return ApiResponse.<ExperienceSkillResponse>builder()
                .message("Create experience skill successfully")
                .data(experienceSkillService.create(resumeId, experienceDetailId, request))
                .build();
    }

    @PutMapping("/{resumeId}/experience-skills/{experienceSkillId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<ExperienceSkillResponse> updateExperienceSkill(
            @PathVariable Integer resumeId,
            @PathVariable Integer experienceSkillId,
            @RequestBody ExperienceSkillRequest request
    ) {
        return ApiResponse.<ExperienceSkillResponse>builder()
                .message("Update experience skill successfully")
                .data(experienceSkillService.update(resumeId, experienceSkillId, request))
                .build();
    }

    @PostMapping("/{resumeId}/projects")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<ResumeProjectResponse> createProject(
            @PathVariable Integer resumeId,
            @RequestBody UpdateResumeProjectRequest request
    ) {
        return ApiResponse.<ResumeProjectResponse>builder()
                .message("Create resume project successfully")
                .data(resumeProjectService.create(resumeId, request))
                .build();
    }

    @PutMapping("/{resumeId}/projects/{projectId}")
    @PreAuthorize("hasRole('CANDIDATE')")
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

    @PostMapping("/{resumeId}/projects/{projectId}/skills")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<ProjectSkillResponse> createProjectSkill(
            @PathVariable Integer resumeId,
            @PathVariable Integer projectId,
            @RequestBody UpdateProjectSkillRequest request
    ) {
        return ApiResponse.<ProjectSkillResponse>builder()
                .message("Create project skill successfully")
                .data(projectSkillService.create(resumeId, projectId, request))
                .build();
    }

    @PutMapping("/{resumeId}/project-skills/{projectSkillId}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<ProjectSkillResponse> updateProjectSkill(
            @PathVariable Integer resumeId,
            @PathVariable Integer projectSkillId,
            @RequestBody UpdateProjectSkillRequest request
    ) {
        return ApiResponse.<ProjectSkillResponse>builder()
                .message("Update project skill successfully")
                .data(projectSkillService.update(resumeId, projectSkillId, request))
                .build();
    }

    @PostMapping("/{resumeId}/certifications")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<ResumeCertificationDetailResponse> createCertification(
            @PathVariable Integer resumeId,
            @RequestBody UpdateResumeCertificationRequest request
    ) {
        return ApiResponse.<ResumeCertificationDetailResponse>builder()
                .message("Create resume certification successfully")
                .data(resumeCertificationService.create(resumeId, request))
                .build();
    }

    @PutMapping("/{resumeId}/certifications/{certificationId}")
    @PreAuthorize("hasRole('CANDIDATE')")
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

    @GetMapping("/{resumeId}")
    public ApiResponse<ResumeDetailResponse> getResumeDetail(@PathVariable Integer resumeId) {
        return ApiResponse.<ResumeDetailResponse>builder()
                .message("Get resume detail successfully")
                .data(resumeService.getResumeDetail(resumeId))
                .build();
    }

    @PostMapping("/{resumeId}/set-profile")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<ResumeResponse> setResumeAsProfile(@PathVariable Integer resumeId) {
        return ApiResponse.<ResumeResponse>builder()
                .message("Set resume as profile successfully")
                .data(resumeService.cloneResume(resumeId, ResumeType.PROFILE))
                .build();
    }

    @PostMapping("/{resumeId}/parse")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<ResumeResponse> parseResume(@PathVariable Integer resumeId) {
        return ApiResponse.<ResumeResponse>builder()
                .message("Parse resume enqueued successfully")
                .data(resumeService.parseResume(resumeId))
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
}
