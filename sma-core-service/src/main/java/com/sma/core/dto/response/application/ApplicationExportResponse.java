package com.sma.core.dto.response.application;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplicationExportResponse {
    String fullName;
    String email;
    String phone;
    String appliedAt;
    String jobTitle;
    Float aiScore;
    String matchLevel;
    String aiSummary;
    Double totalExperienceYears;
    String topSkills;
    String location;
    String resumeUrl;
}
