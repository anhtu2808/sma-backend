package com.sma.core.dto.response.evaluation;

import com.sma.core.enums.EvaluationStatus;
import com.sma.core.enums.EvaluationType;
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
public class ResumeEvaluationOverviewResponse {
    Integer id;
    Float aiOverallScore;
    Float recruiterOverallScore;
    MatchLevel matchLevel;
    String summary;
    String strengths;
    String weakness;
    EvaluationStatus evaluationStatus;
    EvaluationType evaluationType;
    Integer resumeId;
    String resumeFullName;
    String candidateName;
    Integer jobId;
    String jobName;
    List<EvaluationCriteriaScoreResponse> criteriaScores;
}
