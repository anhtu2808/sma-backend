package com.sma.core.dto.response.question;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
public class JobQuestionResponse {
    Integer id;
    String question;
    Boolean isRequired;
    String description;
}
