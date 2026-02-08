package com.sma.core.dto.response.user;

import com.sma.core.dto.response.resume.ResumeResponse;
import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CandidateDetailResponse {

    String linkedinUrl;
    String githubUrl;
    String websiteUrl;
    LocalDate availabilityDate;
    Boolean isProfilePublic;
    List<ResumeResponse> resumes;
}
