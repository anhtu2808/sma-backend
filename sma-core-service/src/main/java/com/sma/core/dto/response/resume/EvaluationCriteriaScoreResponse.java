package com.sma.core.dto.response.resume;

import com.sma.core.enums.CriteriaType;
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
public class EvaluationCriteriaScoreResponse {
    Integer id;
    Integer scoringCriteriaId;
    String scoringCriteriaContext;
    Double scoringCriteriaWeight;
    Integer criteriaId;
    String criteriaName;
    CriteriaType criteriaType;
    Float maxScore;
    Float aiScore;
    Float manualScore;
    Float weightedScore;
    String aiExplanation;
    String manualExplanation;
    List<EvaluationHardSkillResponse> hardSkills;
    List<EvaluationSoftSkillResponse> softSkills;
    List<EvaluationExperienceDetailResponse> experienceDetails;
}
