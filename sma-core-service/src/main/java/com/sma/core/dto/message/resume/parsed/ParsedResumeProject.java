package com.sma.core.dto.message.resume.parsed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sma.core.enums.ProjectType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParsedResumeProject {
    String title;
    Integer teamSize;
    String position;
    String description;
    ProjectType projectType;
    LocalDate startDate;
    LocalDate endDate;
    Boolean isCurrent;
    String projectUrl;
    Integer orderIndex;

    @Builder.Default
    List<ParsedProjectSkill> skills = new ArrayList<>();
}
