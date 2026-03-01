package com.sma.core.service;

import com.sma.core.dto.model.QuotaOwnerContext;
import com.sma.core.dto.request.subscription.CreateSubscriptionRequest;
import com.sma.core.entity.Subscription;
import com.sma.core.enums.Role;

import java.time.LocalDateTime;
import java.util.List;

public interface SubscriptionService {

    String createSubscription(CreateSubscriptionRequest request);

    String createSubscription(Integer targetId, CreateSubscriptionRequest request, Role role);

    void assignDefaultPlanForCandidate(Integer candidateId);

    List<Subscription> findEligibleSubscriptions(QuotaOwnerContext ownerContext, LocalDateTime now);

    List<Subscription> findAllSubscriptions(QuotaOwnerContext ownerContext);

}
