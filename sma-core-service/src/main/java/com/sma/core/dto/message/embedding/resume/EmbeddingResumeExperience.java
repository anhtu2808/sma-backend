package com.sma.core.dto.message.embedding.resume;

import com.sma.core.enums.EmploymentType;
import com.sma.core.enums.WorkingModel;
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
public class EmbeddingResumeExperience {

    String company;
    WorkingModel workingModel;
    LocalDate startDate;
    LocalDate endDate;
    EmploymentType employmentType;
    Set<EmbeddingExperienceDetail> details;
}
