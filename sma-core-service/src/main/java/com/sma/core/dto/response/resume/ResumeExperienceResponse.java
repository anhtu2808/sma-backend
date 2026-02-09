package com.sma.core.dto.response.resume;

import com.sma.core.enums.EmploymentType;
import com.sma.core.enums.WorkingModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResumeExperienceResponse {
    Integer id;
    String company;
    LocalDate startDate;
    LocalDate endDate;
    Boolean isCurrent;
    WorkingModel workingModel;
    EmploymentType employmentType;
    Integer orderIndex;
    List<ResumeExperienceDetailResponse> details;
}
