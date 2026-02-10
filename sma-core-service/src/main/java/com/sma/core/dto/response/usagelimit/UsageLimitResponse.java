package com.sma.core.dto.response.usagelimit;

import com.sma.core.enums.UsageLimitUnit;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UsageLimitResponse {
    Integer featureId;
    String featureKey;
    String featureName;
    Integer maxQuota;
    UsageLimitUnit limitUnit;
}
