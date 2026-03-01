package com.sma.core.service.quota;

import com.sma.core.dto.model.QuotaAggregate;
import com.sma.core.dto.model.QuotaOwnerContext;
import com.sma.core.entity.Feature;
import com.sma.core.entity.Subscription;
import com.sma.core.entity.UsageLimit;

import java.util.List;

public interface QuotaAggregationEngine {
    public QuotaAggregate aggregate(
            Feature feature,
            List<Subscription> subscriptions,
            List<UsageLimit> limits,
            QuotaOwnerContext ownerContext
    );
}
