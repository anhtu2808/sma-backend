package com.sma.core.dto.response.skill;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class SkillCateResponse {
    Integer id;
    String name;
    String description;
    SkillCategoryResponse category;
}
