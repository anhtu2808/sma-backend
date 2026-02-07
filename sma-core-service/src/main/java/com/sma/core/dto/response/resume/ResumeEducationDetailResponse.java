package com.sma.core.dto.response.resume;

import com.sma.core.enums.DegreeType;
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
public class ResumeEducationDetailResponse {
    Integer id;
    String institution;
    DegreeType degree;
    String majorField;
    Double gpa;
    LocalDate startDate;
    LocalDate endDate;
    Boolean isCurrent;
}
