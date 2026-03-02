package com.sma.core.controller;

import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.resume.ResumeEvaluationResponse;
import com.sma.core.service.ResumeEvaluationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/v1/matching")
@RequiredArgsConstructor
public class ResumeEvaluationController {

    ResumeEvaluationService resumeEvaluationService;

    @PostMapping
    public ApiResponse<Void> processMatching(@RequestParam Integer jobId, @RequestParam Integer resumeId){
        resumeEvaluationService.processMatching(jobId, resumeId);
        return ApiResponse.<Void>builder()
                .message("Matching is processing")
                .build();
    }

    @GetMapping("/{resumeEvaluationId}")
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

}

