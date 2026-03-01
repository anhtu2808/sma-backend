package com.sma.core.dto.request.job;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateThresholdRequest {

    Set<AddJobScoringCriteriaRequest> scoringCriteria;

}
