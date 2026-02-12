package com.sma.core.dto.response.application;

import com.sma.core.dto.response.resume.ResumeDetailResponse;
import com.sma.core.dto.response.resume.ResumeEvaluationResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplicationDetailResponse {
    ApplicationResponse applicationInfo;
    ResumeDetailResponse resumeDetail;
    ResumeEvaluationResponse aiEvaluation;
}
