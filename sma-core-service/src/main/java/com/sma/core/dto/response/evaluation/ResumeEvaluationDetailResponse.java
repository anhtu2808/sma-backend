package com.sma.core.dto.response.evaluation;

import com.sma.core.enums.RelevanceType;
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
public class ResumeEvaluationDetailResponse extends ResumeEvaluationOverviewResponse {
    Boolean isTrueLevel;
    Boolean hasRelatedExperience;
    Boolean isSpecificJd;
    RelevanceType transferabilityToRole;
//    List<EvaluationGapResponse> gaps;
//    List<EvaluationWeaknessResponse> weaknesses;
}
