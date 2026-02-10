package com.sma.core.dto.request.application;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplicationRequest {
    @NotNull(message = "JOB_ID_REQUIRED")
    Integer jobId;

    @NotNull(message = "RESUME_ID_REQUIRED")
    Integer resumeId;

    @NotBlank(message = "FULL_NAME_REQUIRED")
    String fullName;

    @NotBlank(message = "PHONE_REQUIRED")
    String phone;

    @Email(message = "INVALID_EMAIL")
    String email;

    String coverLetter;

    LocalDateTime appliedAt;

    List<AnswerRequest> answers;
}
