package com.sma.core.service.impl;

import com.sma.core.entity.Subscription;
import com.sma.core.enums.PaymentMethod;
import com.sma.core.service.PaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${sepay.link}")
    String sePayLink;
    @Value("${sepay.api-key}")
    String sePayApiKey;
    @Value("${sepay.prefix}")
    String sePayPrefix;
    @Value("${sepay.bank-account}")
    String sePayBankAccount;
    @Value("${sepay.bank}")
    String sePayBank;

    @Override
    public String createQR(Subscription subscription, PaymentMethod method) {
        if (PaymentMethod.SEPAY.equals(method) || PaymentMethod.BANK_TRANSFER.equals(method)) {
            long amount = subscription.getPrice().longValue();
            String description = sePayPrefix + " " + subscription.getId();
            return String.format(sePayLink, sePayBankAccount, sePayBank, amount,
                    java.net.URLEncoder.encode(description, java.nio.charset.StandardCharsets.UTF_8));
        }
        return "";
    }
}
