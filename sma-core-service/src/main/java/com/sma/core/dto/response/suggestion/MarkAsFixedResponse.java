package com.sma.core.dto.response.suggestion;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MarkAsFixedResponse {

    Float afterOverallScore;
    Float afterCriteriaScore;

}
