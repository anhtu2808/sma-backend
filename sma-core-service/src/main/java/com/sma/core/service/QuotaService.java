package com.sma.core.service;

import com.sma.core.dto.model.UsageContextModel;
import com.sma.core.dto.model.QuotaOwnerContext;
import com.sma.core.entity.Subscription;
import com.sma.core.entity.UsageEvent;
import com.sma.core.enums.FeatureKey;
import com.sma.core.enums.EventSource;
import com.sma.core.enums.Role;

import java.time.LocalDateTime;
import java.util.List;

public interface QuotaService {

    QuotaOwnerContext resolveOwnerContext();

    QuotaOwnerContext resolveUsageHistoryOwnerContext();

    boolean hasBooleanEntitlement(List<com.sma.core.entity.Subscription> subscriptions, Integer featureId);

    long getStateEffectiveLimit(List<Subscription> subscriptions, Integer featureId);

    EventReservation reserveEventQuota(
            List<com.sma.core.entity.Subscription> subscriptions,
            Integer featureId,
            int amount,
            LocalDateTime now
    );

    UsageEvent commitReservation(EventReservation reservation);

    /**
     * Commit reservation with context information
     */
    UsageEvent commitReservation(EventReservation reservation, EventSource entityType, Integer entityId);

    UsageEvent commitReservation(EventReservation reservation, List<UsageContextModel> contexts);

    /**
     * Validate boolean entitlement (on/off feature)
     */
    void validateBooleanQuota(FeatureKey featureKey);

    /**
     * Validate state quota (count-based like CV_UPLOAD_LIMIT)
     */
    void validateStateQuota(FeatureKey featureKey, Object input);


    /**
     * Consume event quota with context (reserve + commit in one call)
     */
    UsageEvent consumeEventQuota(FeatureKey featureKey, int amount, EventSource entityType, Integer entityId);

    UsageEvent consumeEventQuota(FeatureKey featureKey, int amount, List<UsageContextModel> contexts);

    void markUsageEventFailed(Integer usageEventId);

    /**
     * Check if event quota is available without consuming it
     */
    void checkEventQuotaAvailability(FeatureKey featureKey);
    void checkEventQuotaAvailability(FeatureKey featureKey, Role role, Integer relatedActorId);

    record EventReservation(Integer subscriptionId, Integer featureId, Integer amount) {
    }
}
