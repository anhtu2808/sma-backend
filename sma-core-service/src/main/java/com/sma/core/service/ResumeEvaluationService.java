package com.sma.core.service;

import com.sma.core.dto.message.matching.MatchingResultMessage;
import com.sma.core.dto.message.resume.ResumeParsingResultMessage;
import com.sma.core.dto.request.evaluation.ManualScoreMatchingRequest;
import com.sma.core.dto.response.resume.ResumeEvaluationResponse;

public interface ResumeEvaluationService {

    Integer processMatching(Integer jobId, Integer resumeId);
    Integer processMatchingOverview(Integer jobId, Integer resumeId);
    String getMatchingStatus(Integer id);
    ResumeEvaluationResponse getMatchingResult(Integer id);
    ResumeEvaluationResponse getResumeEvaluation(Integer jobId, Integer resumeId);
    void processMatchingResult(MatchingResultMessage message);
    void generateSuggestion(Integer id);
    void reGenerateSuggestion(Integer id, Integer evaluationWeaknessId);
    ResumeEvaluationResponse scoreManual(Integer id, ManualScoreMatchingRequest request);
}
