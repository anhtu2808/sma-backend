package com.sma.core.service.impl;

import com.sma.core.dto.request.payment.SePayWebhookRequest;
import com.sma.core.entity.PaymentHistory;
import com.sma.core.entity.Subscription;
import com.sma.core.enums.PaymentMethod;
import com.sma.core.enums.PaymentStatus;
import com.sma.core.enums.PlanType;
import com.sma.core.enums.SubscriptionStatus;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.repository.PaymentRepository;
import com.sma.core.repository.SubscriptionRepository;
import com.sma.core.service.PaymentService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@Slf4j
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

    final PaymentRepository paymentRepository;
    final SubscriptionRepository subscriptionRepository;

    @Override
    public String createQR(Subscription subscription, PaymentMethod method) {
        PaymentHistory paymentHistory = PaymentHistory.builder()
                .amount(subscription.getPrice())
                .subscription(subscription)
                .paymentStatus(PaymentStatus.PENDING)
                .currency(subscription.getPlan().getCurrency())
                .build();
        long amount = subscription.getPrice()
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
        String description = sePayPrefix + " ";
        if (PaymentMethod.SEPAY.equals(method) || PaymentMethod.BANK_TRANSFER.equals(method)) {
            paymentHistory.setPaymentMethod(method);
            paymentRepository.saveAndFlush(paymentHistory);
            description += paymentHistory.getId();
            return String.format(sePayLink, sePayBankAccount, sePayBank, amount,
                    java.net.URLEncoder.encode(description, java.nio.charset.StandardCharsets.UTF_8));
        }
        return "";
    }

    @Override
    @Transactional
    public Boolean confirm(String authorization, SePayWebhookRequest request) {
        if (!sePayApiKey.equals(authorization)) {
            throw new AppException(ErrorCode.NOT_HAVE_PERMISSION);
        }
        String content = request.getContent();
        if (!content.startsWith(sePayPrefix)) {
            throw new AppException(ErrorCode.INVALID_SEPAY_CONTENT_FORMAT);
        }
        Integer id = Integer.parseInt(
                content.substring(sePayPrefix.length()).trim()
        );
        PaymentHistory paymentHistory = paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        LocalDateTime expiredTime = paymentHistory.getCreatedAt().plusMinutes(15);
        if (request.getTransactionDate().isAfter(expiredTime)) {
            paymentHistory.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(paymentHistory);
            throw new AppException(ErrorCode.PAYMENT_TIME_EXPIRED);
        }
        if (paymentHistory.getPaymentStatus() == PaymentStatus.SUCCESS) {
            return true;
        }
        paymentHistory.setPaidAt(request.getTransactionDate());
        paymentHistory.setPaymentStatus(PaymentStatus.SUCCESS);
        paymentHistory.setTransactionCode(String.valueOf(request.getId()));
        Subscription subscription = paymentHistory.getSubscription();
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setPurchasedAt(request.getTransactionDate());
        if (subscription.getPlan() != null && subscription.getPlan().getPlanType() == PlanType.MAIN) {
            LocalDateTime now = request.getTransactionDate();
            if (subscription.getCandidate() != null) {
                subscriptionRepository.expireActiveMainByCandidateId(
                        subscription.getCandidate().getId(),
                        SubscriptionStatus.ACTIVE,
                        SubscriptionStatus.EXPIRED,
                        PlanType.MAIN,
                        now,
                        now,
                        subscription.getId()
                );
            } else if (subscription.getCompany() != null) {
                subscriptionRepository.expireActiveMainByCompanyId(
                        subscription.getCompany().getId(),
                        SubscriptionStatus.ACTIVE,
                        SubscriptionStatus.EXPIRED,
                        PlanType.MAIN,
                        now,
                        now,
                        subscription.getId()
                );
            }
        }
        paymentRepository.save(paymentHistory);
        return true;
    }
}
