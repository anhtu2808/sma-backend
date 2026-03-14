package com.sma.core.dto.response.evaluation;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ResumeEvaluationHistoryResponse {

    Integer id;
    Float overallScore;

}
