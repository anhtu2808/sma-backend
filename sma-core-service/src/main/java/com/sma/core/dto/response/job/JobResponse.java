package com.sma.core.dto.response.job;

import com.sma.core.dto.response.company.CompanyResponse;
import com.sma.core.dto.response.skill.SkillResponse;
import com.sma.core.entity.*;
import com.sma.core.enums.JobLevel;
import com.sma.core.enums.JobStatus;
import com.sma.core.enums.WorkingModel;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class JobResponse {

    Integer id;
    String name;
    String about;
    String responsibilities;
    String requirement;
    Boolean isViolated;
    LocalDateTime uploadTime;
    LocalDateTime expDate;
    BigDecimal salaryStart;
    BigDecimal salaryEnd;
    String currency;
    Integer experienceTime;
    JobStatus status;
    JobLevel jobLevel;
    WorkingModel workingModel;
    Integer quantity;
    Double autoRejectThreshold;
    JobResponse rootJob;
    CompanyResponse company;
    Set<SkillResponse> skills;
    Set<DomainResponse> domains;
    Set<BenefitResponse> benefits;
}
