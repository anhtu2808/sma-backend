package com.sma.core.service;

import com.sma.core.dto.message.matching.MatchingResultMessage;
import com.sma.core.dto.request.evaluation.ManualScoreMatchingRequest;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.evaluation.ResumeEvaluationDetailResponse;
import com.sma.core.dto.response.evaluation.ResumeEvaluationOverviewResponse;
import org.springframework.data.domain.Pageable;

public interface ResumeEvaluationService {

    Integer processMatching(Integer jobId, Integer resumeId);
    Integer processMatchingOverview(Integer jobId, Integer resumeId);
    String getMatchingStatus(Integer id);
    ResumeEvaluationDetailResponse getEvaluationDetail(Integer id);
    PagingResponse<ResumeEvaluationOverviewResponse> getEvaluationsByJob(Integer jobId, Pageable pageable);
    PagingResponse<ResumeEvaluationOverviewResponse> getAllEvaluations(Pageable pageable);
    void processMatchingResult(MatchingResultMessage message);
    void generateSuggestion(Integer id);
    void reGenerateSuggestion(Integer id, Integer evaluationWeaknessId);
    ResumeEvaluationDetailResponse scoreManual(Integer id, ManualScoreMatchingRequest request);
}
