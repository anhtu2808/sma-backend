package com.sma.core.service;

import com.sma.core.dto.model.QuotaOwnerContext;
import com.sma.core.entity.Subscription;
import com.sma.core.enums.FeatureKey;
import com.sma.core.enums.EventSource;

import java.time.LocalDateTime;
import java.util.List;

public interface QuotaService {

    QuotaOwnerContext resolveOwnerContext();

    boolean hasBooleanEntitlement(List<com.sma.core.entity.Subscription> subscriptions, Integer featureId);

    long getStateEffectiveLimit(List<Subscription> subscriptions, Integer featureId);

    EventReservation reserveEventQuota(
            List<com.sma.core.entity.Subscription> subscriptions,
            Integer featureId,
            int amount,
            LocalDateTime now
    );

    void commitReservation(EventReservation reservation);

    /**
     * Commit reservation with context information
     */
    void commitReservation(EventReservation reservation, EventSource entityType, Integer entityId);

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
    void consumeEventQuota(FeatureKey featureKey, int amount, EventSource entityType, Integer entityId);

    /**
     * Check if event quota is available without consuming it
     */
    void checkEventQuotaAvailability(FeatureKey featureKey);

    record EventReservation(Integer subscriptionId, Integer featureId, Integer amount) {
    }
}
