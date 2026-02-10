package com.sma.core.dto.response.application;

import com.sma.core.enums.ApplicationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplicationResponse {
    Integer id;
    ApplicationStatus status;
    Integer attempt;
    String fullName;
    String email;
    String phone;
    String coverLetter;
    LocalDateTime appliedAt;
    Integer jobId;
    String jobTitle;
    Integer resumeId;
    String resumeName;

    List<JobAnswerResponse> answers;
}
