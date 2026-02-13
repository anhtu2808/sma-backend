package com.sma.core.dto.response.featureusage;

import com.sma.core.enums.UsageLimitUnit;
import com.sma.core.enums.UsageType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeatureUsageResponse {
    Integer featureId;
    String featureKey;
    String featureName;
    UsageType usageType;
    UsageLimitUnit limitUnit;
    Long maxQuota;
    Long used;
    Long remaining;
}
