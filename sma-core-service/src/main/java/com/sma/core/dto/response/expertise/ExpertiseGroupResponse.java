package com.sma.core.dto.response.expertise;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpertiseGroupResponse {
    Integer id;
    String name;
    String description;
}
