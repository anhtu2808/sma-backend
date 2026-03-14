package com.sma.core.dto.message.embedding.resume;

import com.sma.core.enums.ProjectType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmbeddingResumeProject {

    String title;
    Integer teamSize;
    ProjectType projectType;
    LocalDate startDate;
    LocalDate endDate;
    String position;
    String description;
    Set<EmbeddingExperienceSkill> projectSkills;

}
