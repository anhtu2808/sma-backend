package com.sma.core.service;

import com.sma.core.dto.request.payment.SePayWebhookRequest;
import com.sma.core.dto.response.payment.CreatePaymentResponse;
import com.sma.core.entity.Subscription;
import com.sma.core.enums.PaymentMethod;
import com.sma.core.enums.PaymentStatus;

public interface PaymentService {

    CreatePaymentResponse createQR(Subscription subscription, PaymentMethod method);
    Boolean confirm(String authorization, SePayWebhookRequest request);
    PaymentStatus getPaymentStatus(Integer id);

}
