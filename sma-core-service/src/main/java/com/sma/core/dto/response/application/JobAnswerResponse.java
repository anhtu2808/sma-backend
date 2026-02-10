package com.sma.core.dto.response.application;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobAnswerResponse {
    Integer questionId;
    String questionText;
    String answerContent;
}
