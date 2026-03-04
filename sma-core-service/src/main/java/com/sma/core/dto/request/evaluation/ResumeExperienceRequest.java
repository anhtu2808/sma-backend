package com.sma.core.dto.request.evaluation;

import com.sma.core.enums.EmploymentType;
import com.sma.core.enums.WorkingModel;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResumeExperienceRequest {

    String description;
    String company;
    WorkingModel workingModel;
    EmploymentType employmentType;
    Set<ResumeExperienceDetailRequest> details;
}
