package com.sma.core.dto.response.quota;

import com.sma.core.enums.UsageLimitUnit;
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
public class QuotaConsumptionResponse {
    String featureKey;
    UsageLimitUnit limitUnit;
    Long maxQuota;
    Long totalUsed;
    Long jobUsed;
    Long remaining;
}
