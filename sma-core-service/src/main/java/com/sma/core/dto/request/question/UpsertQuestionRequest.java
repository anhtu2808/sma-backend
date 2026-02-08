package com.sma.core.dto.request.question;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UpsertQuestionRequest {

    @NotNull(message = "Question must not be null")
    @NotBlank(message = "Question must not be blank")
    @Size(max = 255, message = "Question must be less than 255 characters")
    String question;

    @NotNull(message = "Is required must not be null")
    Boolean isRequired;

    @Size(max = 255, message = "Description must be less than 255 characters")
    String description;
}
