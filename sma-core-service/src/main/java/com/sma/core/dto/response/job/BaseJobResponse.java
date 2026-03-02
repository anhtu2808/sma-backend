package com.sma.core.dto.response.job;

import com.sma.core.dto.response.company.BaseCompanyResponse;
import com.sma.core.dto.response.company.CompanyLocationResponse;
import com.sma.core.dto.response.question.JobQuestionResponse;
import com.sma.core.dto.response.skill.SkillResponse;
import com.sma.core.entity.CompanyLocation;
import com.sma.core.enums.ApplicationStatus;
import com.sma.core.enums.JobLevel;
import com.sma.core.enums.JobStatus;
import com.sma.core.enums.WorkingModel;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
public class BaseJobResponse {
    Integer id;
    String name;
    LocalDateTime uploadTime;
    LocalDateTime expDate;
    BigDecimal salaryStart;
    BigDecimal salaryEnd;
    String currency;
    Integer experienceTime;
    JobStatus status;
    JobLevel jobLevel;
    WorkingModel workingModel;
    JobExpertiseResponse expertise;
    BaseCompanyResponse company;
    Set<SkillResponse> skills;
    Set<DomainResponse> domains;
    Set<BenefitResponse> benefits;
    Set<JobQuestionResponse> questions;
    Boolean isApplied;
    LocalDateTime lastApplyAt;
    ApplicationStatus applicationStatus;
    String appliedResumeUrl;
    Set<CompanyLocationResponse> locations;
    Boolean isSample;
}
