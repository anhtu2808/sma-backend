package com.sma.core.dto.request.job;

import com.sma.core.entity.*;
import com.sma.core.enums.Currency;
import com.sma.core.enums.JobLevel;
import com.sma.core.enums.WorkingModel;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PublishJobRequest {

    @NotBlank(message = "Job name must not be blank")
    @Size(max = 255, message = "Job name must be less than 255 characters")
    String name;

    @NotBlank(message = "About must not be blank")
    String about;

    @NotBlank(message = "Responsibilities must not be blank")
    String responsibilities;

    @NotBlank(message = "Requirement must not be blank")
    String requirement;

    @NotNull(message = "Enable AI scoring must not be null")
    Boolean enableAiScoring;

    @NotNull(message = "Expiration date must not be null")
    @Future(message = "Expiration date must be in the future")
    LocalDateTime expDate;

    @NotNull(message = "Salary start must not be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Salary start must be greater than 0")
    BigDecimal salaryStart;

    @NotNull(message = "Salary end must not be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Salary end must be greater than 0")
    BigDecimal salaryEnd;

    @NotNull(message = "Currency must not be null")
    Currency currency;

    @Min(value = 0, message = "Experience time must be >= 0")
    @Max(value = 50, message = "Experience time must be <= 50 years")
    Integer experienceTime;

    @NotNull(message = "Job level must not be null")
    JobLevel jobLevel;

    @NotNull(message = "Working model must not be null")
    WorkingModel workingModel;

    @NotNull(message = "Quantity must not be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity;

    @DecimalMin(value = "0.0", message = "Auto reject threshold must be >= 0")
    @DecimalMax(value = "100.0", message = "Auto reject threshold must be <= 100")
    Double autoRejectThreshold;

    @NotNull(message = "Expertise must not be null")
    Integer expertiseId;

    @Size(max = 50, message = "Skills must be less than 50")
    List<Integer> skillIds;

    @Size(max = 20, message = "Domains must be less than 20")
    List<Integer> domainIds;

    @Size(max = 20, message = "Benefits must be less than 20")
    List<Integer> benefitIds;

    @Size(max = 20, message = "Questions must be less than 20")
    List<Integer> questionIds;

    @Size(min = 2, message = "Scoring criterias must be greater than 2")
    Set<AddJobScoringCriteriaRequest> scoringCriterias;

    @NotEmpty(message = "At least one location is required")
    List<Integer> locationIds;

    Integer rootId;

}
