package com.sma.core.dto.request.resume;

import com.sma.core.enums.ProjectType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateResumeProjectRequest {
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
}
