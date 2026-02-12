package com.sma.core.service;

import com.sma.core.entity.Feature;
import com.sma.core.entity.Subscription;
import com.sma.core.enums.FeatureKey;
import com.sma.core.service.quota.QuotaOwnerContext;

import java.time.LocalDateTime;
import java.util.List;

public interface FeatureQuotaRuntimeService {
    QuotaOwnerContext resolveOwnerContext();

    Feature resolveActiveFeature(FeatureKey featureKey);

    List<Subscription> findEligibleSubscriptions(QuotaOwnerContext ownerContext, LocalDateTime now);

    boolean hasBooleanEntitlement(List<Subscription> subscriptions, Integer featureId);

    long getStateEffectiveLimit(List<Subscription> subscriptions, Integer featureId);

    EventReservation selectEventReservation(List<Subscription> subscriptions,
                                            Integer featureId,
                                            int amount,
                                            LocalDateTime now);

    void saveUsageEvent(EventReservation reservation);

    record EventReservation(Integer subscriptionId, Integer featureId, Integer amount) {
    }
}
