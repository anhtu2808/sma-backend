package com.sma.core.service;

import com.sma.core.dto.request.subscription.CreateSubscriptionRequest;
import com.sma.core.entity.Subscription;

public interface SubscriptionService {

    String createSubscription(CreateSubscriptionRequest request);

}
