package com.sma.core.dto.response.skill;

import com.sma.core.entity.SkillCategory;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class SkillResponse {
    Integer id;
    String name;
    String description;
//    SkillCategory category;
}
