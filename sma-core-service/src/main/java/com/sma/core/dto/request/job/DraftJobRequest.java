package com.sma.core.dto.request.job;

import com.sma.core.entity.JobExpertise;
import com.sma.core.enums.Currency;
import com.sma.core.enums.JobLevel;
import com.sma.core.enums.WorkingModel;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftJobRequest {

    private String name;
    private String about;
    private String responsibilities;
    private String requirement;
    private Boolean enableAiScoring;
    private LocalDateTime expDate;
    private BigDecimal salaryStart;
    private BigDecimal salaryEnd;
    private Currency currency;
    private Integer experienceTime;
    private JobLevel jobLevel;
    private WorkingModel workingModel;
    private Integer quantity;
    private Double autoRejectThreshold;
    private Integer expertiseId;
    private List<Integer> skillIds;
    private List<Integer> domainIds;
    private List<Integer> benefitIds;
    private List<Integer> questionIds;
    private Set<AddJobScoringCriteriaRequest> scoringCriteriaIds;
    private List<Integer> locationIds;


}
