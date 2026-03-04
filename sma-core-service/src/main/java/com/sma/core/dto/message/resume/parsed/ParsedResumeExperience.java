package com.sma.core.dto.message.resume.parsed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sma.core.enums.EmploymentType;
import com.sma.core.enums.WorkingModel;
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
public class ParsedResumeExperience {
    String company;
    LocalDate startDate;
    LocalDate endDate;
    Boolean isCurrent;
    WorkingModel workingModel;
    EmploymentType employmentType;
    Integer orderIndex;

    @Builder.Default
    List<ParsedResumeExperienceDetail> details = new ArrayList<>();
}
