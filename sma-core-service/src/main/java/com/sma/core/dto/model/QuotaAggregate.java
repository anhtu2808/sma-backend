package com.sma.core.dto.model;

import com.sma.core.entity.Feature;
import com.sma.core.enums.UsageLimitUnit;
import com.sma.core.enums.UsageType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuotaAggregate {

    Integer featureId;
    String featureKey;
    String featureName;
    UsageType usageType;
    UsageLimitUnit limitUnit;

    long maxQuota;
    long used;
    long remaining;

    public static QuotaAggregate booleanFeature(Feature feature) {
        return QuotaAggregate.builder()
                .featureId(feature.getId())
                .featureKey(feature.getFeatureKey())
                .featureName(feature.getName())
                .usageType(UsageType.BOOLEAN)
                .build();
    }
}
