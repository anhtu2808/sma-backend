package com.sma.core.dto.request.question;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class JobQuestionFilterRequest {
    String keyword;
    Boolean deleted;

    Integer page;
    Integer size;
}
