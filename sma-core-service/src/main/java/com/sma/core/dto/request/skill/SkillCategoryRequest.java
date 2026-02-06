package com.sma.core.dto.request.skill;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SkillCategoryRequest {
    @NotBlank(message = "Skill Category name is required")
    String name;
}
