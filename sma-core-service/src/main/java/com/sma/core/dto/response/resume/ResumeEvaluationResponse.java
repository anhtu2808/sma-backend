package com.sma.core.dto.response.resume;

import com.sma.core.enums.EvaluationStatus;
import com.sma.core.enums.MatchLevel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResumeEvaluationResponse {
    Integer id;
    Float aiOverallScore;
    Float recruiterOverallScore;
    MatchLevel matchLevel;
    String summary;
    String strengths;
    String weakness;
    Boolean isTrueLevel;
    Boolean hasRelatedExperience;
    Boolean isSpecificJd;
    EvaluationStatus evaluationStatus;
    Float processingTimeSecond;
    String aiModelVersion;
    Integer jobId;
    String jobName;
    List<EvaluationCriteriaScoreResponse> criteriaScores;
    List<EvaluationGapResponse> gaps;
    List<EvaluationWeaknessResponse> weaknesses;
}
