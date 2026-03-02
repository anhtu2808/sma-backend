package com.sma.core.dto.request.evaluation;

import com.sma.core.enums.ProjectType;
import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResumeProjectRequest {

    String title;
    Integer teamSize;
    String description;
    ProjectType projectType;
    Set<ResumeExperienceSkillRequest> skills;

}
