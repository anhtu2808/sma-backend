package com.sma.core.service.quota;

import com.sma.core.entity.Subscription;
import com.sma.core.enums.UsageEntityType;
import com.sma.core.enums.UsageLimitUnit;

import java.util.List;

public interface EventUsageCalculator {
    long calculate(
            List<Subscription> subscriptions,
            Integer featureId,
            UsageLimitUnit unit,
            UsageEntityType entityType,
            Integer entityId
    );
}