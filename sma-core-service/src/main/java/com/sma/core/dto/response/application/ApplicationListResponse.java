package com.sma.core.dto.response.application;

import com.sma.core.enums.ApplicationStatus;
import com.sma.core.enums.MatchLevel;
import com.sma.core.enums.ResumeLanguage;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplicationListResponse {
     Integer applicationId;
     String candidateName;
     String candidateEmail;
     ApplicationStatus status;
     LocalDateTime appliedAt;
     String resumeUrl;
     String location;
     Double totalExperienceYears;
     List<String> topSkills;
     ResumeLanguage language;
     Float aiScore;
     MatchLevel matchLevel;
     String aiSummary;
}
