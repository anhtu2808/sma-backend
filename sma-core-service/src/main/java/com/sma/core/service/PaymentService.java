package com.sma.core.service;

import com.sma.core.dto.request.payment.SePayWebhookRequest;
import com.sma.core.entity.Subscription;
import com.sma.core.enums.PaymentMethod;

public interface PaymentService {

    String createQR(Subscription subscription, PaymentMethod method);
    Boolean confirm(String authorization, SePayWebhookRequest request);

}
