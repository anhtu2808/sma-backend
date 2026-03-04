package com.sma.core.dto.message.matching;

import com.sma.core.enums.EvaluationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MatchingResultMessage {

    Integer evaluationId;
    EvaluationStatus status;
    String errorMessage;
    String processedAt;
    MatchingResultData parsedData;

}
