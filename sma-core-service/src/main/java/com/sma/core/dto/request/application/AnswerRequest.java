package com.sma.core.dto.request.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnswerRequest {
    @NotNull(message = "QUESTION_ID_REQUIRED")
    Integer questionId;
    String answerContent;
}
