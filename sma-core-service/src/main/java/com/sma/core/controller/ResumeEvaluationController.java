package com.sma.core.controller;

import com.sma.core.dto.request.evaluation.ManualScoreMatchingRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.evaluation.ResumeEvaluationDetailResponse;
import com.sma.core.dto.response.evaluation.ResumeEvaluationOverviewResponse;
import com.sma.core.service.ResumeEvaluationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/v1/matching")
@RequiredArgsConstructor
public class ResumeEvaluationController {

    ResumeEvaluationService resumeEvaluationService;

    @PostMapping("/overall")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<Integer> processMatching(@RequestParam Integer jobId, @RequestParam Integer resumeId){
        return ApiResponse.<Integer>builder()
                .message("Matching is processing")
                .data(resumeEvaluationService.processMatchingOverview(jobId, resumeId))
                .build();
    }

    @PostMapping("/detail")
    @PreAuthorize("hasAnyRole('RECRUITER', 'CANDIDATE')")
    public ApiResponse<Integer> processMatchingDetail(@RequestParam Integer jobId, @RequestParam Integer resumeId){
        return ApiResponse.<Integer>builder()
                .message("Matching detail is processing")
                .data(resumeEvaluationService.processMatching(jobId, resumeId))
                .build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER', 'CANDIDATE')")
    public ApiResponse<PagingResponse<ResumeEvaluationOverviewResponse>> getAllEvaluations(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size){
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.<PagingResponse<ResumeEvaluationOverviewResponse>>builder()
                .message("Get all evaluations successfully")
                .data(resumeEvaluationService.getAllEvaluations(pageable))
                .build();
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER')")
    public ApiResponse<PagingResponse<ResumeEvaluationOverviewResponse>> getEvaluationsByJob(
            @PathVariable Integer jobId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size){
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.<PagingResponse<ResumeEvaluationOverviewResponse>>builder()
                .message("Get evaluations by job successfully")
                .data(resumeEvaluationService.getEvaluationsByJob(jobId, pageable))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER', 'CANDIDATE')")
    public ApiResponse<ResumeEvaluationDetailResponse> getEvaluationDetail(@PathVariable Integer id){
        return ApiResponse.<ResumeEvaluationDetailResponse>builder()
                .message("Get evaluation detail successfully")
                .data(resumeEvaluationService.getEvaluationDetail(id))
                .build();
    }

    @PostMapping("/{resumeEvaluationId}/suggestion")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<Void> generateSuggestion(@PathVariable Integer resumeEvaluationId, @RequestParam Integer suggestionId){
        if (suggestionId == null) {
            resumeEvaluationService.generateSuggestion(resumeEvaluationId);
        } else {
            resumeEvaluationService.reGenerateSuggestion(resumeEvaluationId, suggestionId);
        }
        return ApiResponse.<Void>builder()
                .message(suggestionId == null ?
                        "Generate suggestion is processing" :
                        "Re-generate suggestion is processing")
                .build();
    }

    @GetMapping("/{resumeEvaluationId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER', 'CANDIDATE')")
    public ApiResponse<String> getMatchingStatus(@PathVariable Integer resumeEvaluationId){
        return ApiResponse.<String>builder()
                .message("Get matching status successfully")
                .data(resumeEvaluationService.getMatchingStatus(resumeEvaluationId))
                .build();
    }

    @PutMapping("/{id}/score-manual")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<ResumeEvaluationDetailResponse> scoreManual(@PathVariable Integer id, @RequestBody ManualScoreMatchingRequest request){
        return ApiResponse.<ResumeEvaluationDetailResponse>builder()
                .message("Score manual successfully")
                .data(resumeEvaluationService.scoreManual(id, request))
                .build();
    }

}
