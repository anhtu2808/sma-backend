package com.sma.core.dto.request.evaluation;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ManualScoreMatchingRequest {

    Float manualScore;
    List<ManualScoreCriteriaRequest> scoreCriteriaRequests;
}
