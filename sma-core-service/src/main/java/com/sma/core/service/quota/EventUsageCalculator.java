package com.sma.core.service.quota;

import com.sma.core.entity.Subscription;
import com.sma.core.enums.EventSource;
import com.sma.core.enums.UsageLimitUnit;

import java.util.List;

public interface EventUsageCalculator {
    long calculate(
            List<Subscription> subscriptions,
            Integer featureId,
            UsageLimitUnit unit,
            EventSource entityType,
            Integer entityId
    );
}