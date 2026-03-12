package com.sma.core.dto.response.evaluation;

import com.sma.core.enums.CriteriaType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EvaluationCriteriaScoreResponse {
    Integer id;
    Integer scoringCriteriaId;
    String scoringCriteriaContext;
    String criteriaName;
    Double scoringCriteriaWeight;
    CriteriaType criteriaType;
    Float aiScore;
    Float manualScore;
    Float weightedScore;
    String aiExplanation;
    String manualExplanation;
    Set<EvaluationCriteriaDetailResponse> details;

}
