package com.sma.core.dto.request.job;

import com.sma.core.enums.JobLevel;
import com.sma.core.enums.JobStatus;
import com.sma.core.enums.WorkingModel;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(
            name = "Chỉ có admin, recruiter mới có quyền lọc theo status"
    )
    EnumSet<JobStatus> statuses;

    @Schema(
            name = "Chỉ có admin, recruiter mới có quyền lọc theo company"
    )
    Integer companyId;

    @Builder.Default
    Integer page = 0;

    @Builder.Default
    Integer size = 10;

}
