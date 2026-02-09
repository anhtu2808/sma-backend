package com.sma.core.dto.request.candidate;

import com.sma.core.enums.CandidateShowAs;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCandidateProfileRequest {
    String fullName;
    String avatar;
    String jobTitle;
    String linkedinUrl;
    String githubUrl;
    String websiteUrl;
    String address;
    BigDecimal expectedSalaryMin;
    BigDecimal expectedSalaryMax;
    LocalDate availabilityDate;
    Boolean isProfilePublic;
    CandidateShowAs showAs;
}
