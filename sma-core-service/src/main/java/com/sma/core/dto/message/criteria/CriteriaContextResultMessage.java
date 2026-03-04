package com.sma.core.dto.message.criteria;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CriteriaContextResultMessage {

    Integer jobId;
    String status;  // "SUCCESS" or "FAIL"
    String errorMessage;

    /**
     * Map of criteriaType (e.g. "HARD_SKILLS") -> generated context string.
     */
    Map<String, String> contexts;

    /**
     * Map of criteriaType -> scoringCriteriaId (echoed back from request).
     */
    Map<String, Integer> criteriaTypeToScoringCriteriaId;
}
