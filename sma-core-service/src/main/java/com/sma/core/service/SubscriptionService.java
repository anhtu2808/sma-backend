package com.sma.core.service;

import com.sma.core.dto.request.subscription.CreateSubscriptionRequest;
import com.sma.core.entity.Subscription;
import com.sma.core.enums.Role;

public interface SubscriptionService {

    String createSubscription(CreateSubscriptionRequest request);
    String createSubscription(Integer targetId, CreateSubscriptionRequest request, Role role);
    void assignDefaultPlanForCandidate(Integer candidateId);

}
