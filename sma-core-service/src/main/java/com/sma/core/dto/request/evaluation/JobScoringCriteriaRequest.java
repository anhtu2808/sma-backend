package com.sma.core.dto.request.evaluation;

import com.sma.core.enums.CriteriaType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobScoringCriteriaRequest {

    String context;
    Double weight;
    CriteriaType criteriaType;

}
