package com.sma.core.dto.request.job;

import com.sma.core.enums.JobLevel;
import com.sma.core.enums.JobStatus;
import com.sma.core.enums.WorkingModel;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class JobFilterRequest {

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
    Set<String> location;
    EnumSet<JobStatus> statuses;
    Integer companyId;

    @Builder.Default
    Integer page = 0;

    @Builder.Default
    Integer size = 10;

}
