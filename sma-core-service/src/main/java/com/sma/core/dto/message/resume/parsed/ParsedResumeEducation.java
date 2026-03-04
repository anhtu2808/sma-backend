package com.sma.core.dto.message.resume.parsed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sma.core.enums.DegreeType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParsedResumeEducation {
    String institution;
    DegreeType degree;
    String majorField;
    Double gpa;
    LocalDate startDate;
    LocalDate endDate;
    Boolean isCurrent;
    Integer orderIndex;
}
