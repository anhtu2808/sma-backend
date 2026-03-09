package com.sma.core.dto.message.embedding.resume;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmbeddingExperienceDetail {

    String title;
    String description;
    Set<EmbeddingExperienceSkill> experienceSkills;

}
