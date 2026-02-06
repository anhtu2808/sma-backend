package com.sma.core.dto.response.job;

import com.sma.core.entity.ScoringCriteria;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
public class JobDetailResponse extends BaseJobResponse{

    String about;
    String responsibilities;
    String requirement;
    Boolean isViolated;
    Integer quantity;
    Double autoRejectThreshold;
    BaseJobResponse rootJob;
    Set<JobScoringCriteriaResponse> scoringCriterias;
    Boolean enableAiScoring;

}
