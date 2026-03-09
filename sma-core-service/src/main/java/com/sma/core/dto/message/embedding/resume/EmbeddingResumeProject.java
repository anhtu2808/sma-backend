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
public class EmbeddingResumeProject {

    String title;
    String position;
    String description;
    Set<EmbeddingExperienceSkill> projectSkills;

}
