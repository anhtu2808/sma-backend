package com.sma.core.controller;

import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.suggestion.MarkAsFixedResponse;
import com.sma.core.service.ResumeEvaluationService;
import com.sma.core.service.ScoringCriteriaService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/v1/criteria-score")
@RequiredArgsConstructor
public class EvaluationCriteriaController {

    ResumeEvaluationService resumeEvaluationService;

    @PutMapping("/detail/{detailId}/mark-as-fixed")
    public ApiResponse<MarkAsFixedResponse> markAsFixed(@PathVariable Integer detailId) {
        return ApiResponse.<MarkAsFixedResponse>builder()
                .message("Mark as fixed successfully")
                .data(resumeEvaluationService.markAsFixed(detailId))
                .build();
    }

}
