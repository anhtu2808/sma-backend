package com.sma.core.dto.response.criteria;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CriteriaResponse {

    Integer id;
    String name;
    String rule;
    Double weight;
}
