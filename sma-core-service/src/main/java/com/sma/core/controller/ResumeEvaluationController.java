package com.sma.core.controller;

import com.sma.core.dto.request.evaluation.ManualScoreMatchingRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.resume.ResumeEvaluationResponse;
import com.sma.core.service.ResumeEvaluationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
    public ApiResponse<Integer> processMatching(@RequestParam Integer jobId, @RequestParam Integer resumeId){
        return ApiResponse.<Integer>builder()
                .message("Matching is processing")
                .data(resumeEvaluationService.processMatchingOverview(jobId, resumeId))
                .build();
    }

    @PostMapping("/detail")
    public ApiResponse<Integer> processMatchingDetail(@RequestParam Integer jobId, @RequestParam Integer resumeId){
        return ApiResponse.<Integer>builder()
                .message("Matching detail is processing")
                .data(resumeEvaluationService.processMatching(jobId, resumeId))
                .build();
    }

    @GetMapping
    public ApiResponse<ResumeEvaluationResponse> getResumeEvaluation(@RequestParam Integer jobId, @RequestParam Integer resumeId){
        return ApiResponse.<ResumeEvaluationResponse>builder()
                .message("Get resume evaluation by job id and resume id successfully")
                .data(resumeEvaluationService.getResumeEvaluation(jobId, resumeId))
                .build();
    }

    @GetMapping("/{resumeEvaluationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ResumeEvaluationResponse> getMatchingResult(@PathVariable Integer resumeEvaluationId){
        return ApiResponse.<ResumeEvaluationResponse>builder()
                .message("Get matching result successfully")
                .data(resumeEvaluationService.getMatchingResult(resumeEvaluationId))
                .build();
    }

    @PostMapping("/{resumeEvaluationId}/suggestion")
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
    public ApiResponse<String> getMatchingStatus(@PathVariable Integer resumeEvaluationId){
        return ApiResponse.<String>builder()
                .message("Get matching status successfully")
                .data(resumeEvaluationService.getMatchingStatus(resumeEvaluationId))
                .build();
    }

    @PutMapping("/{id}/score-manual")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<ResumeEvaluationResponse> scoreManual(@PathVariable Integer id, @RequestBody ManualScoreMatchingRequest request){
        return ApiResponse.<ResumeEvaluationResponse>builder()
                .message("Score manual successfully")
                .data(resumeEvaluationService.scoreManual(id, request))
                .build();
    }

}
