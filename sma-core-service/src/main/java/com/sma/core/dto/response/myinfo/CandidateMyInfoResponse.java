package com.sma.core.dto.response.myinfo;

import com.sma.core.enums.CandidateShowAs;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CandidateMyInfoResponse {
    Integer id;
    String jobTitle;
    String linkedinUrl;
    String githubUrl;
    String websiteUrl;
    BigDecimal expectedSalaryMin;
    BigDecimal expectedSalaryMax;
    LocalDate availabilityDate;
    Boolean isProfilePublic;
    CandidateShowAs showAs;
    Integer profileCompleteness;
    UserMyInfoResponse user;
}
