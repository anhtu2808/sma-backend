package com.sma.core.dto.request.skill;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SkillRequest {
    String name;
    String description;
    Integer categoryId;
}
