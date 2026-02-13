package com.sma.core.dto.request.job;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobAiSettingsRequest {
    Boolean enableAiScoring;
    Double autoRejectThreshold;
}
