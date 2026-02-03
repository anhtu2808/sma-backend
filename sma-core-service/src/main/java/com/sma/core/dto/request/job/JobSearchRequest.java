package com.sma.core.dto.request.job;

import com.sma.core.entity.Skill;
import com.sma.core.enums.JobLevel;
import com.sma.core.enums.WorkingModel;
import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class JobSearchRequest {

    String name;
    BigDecimal salaryStart;
    BigDecimal salaryEnd;
    int minExperienceTime;
    int maxExperienceTime;
    JobLevel jobLevel;
    WorkingModel workingModel;
    Set<Integer> skillId;
    Set<Integer> expertiseId;
    Set<Integer> domainId;
    @Builder.Default
    Integer page = 0;

    @Builder.Default
    Integer size = 10;

}
