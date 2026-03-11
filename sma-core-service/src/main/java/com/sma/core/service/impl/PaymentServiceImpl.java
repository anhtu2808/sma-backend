package com.sma.core.service.impl;

import com.sma.core.dto.request.payment.SePayWebhookRequest;
import com.sma.core.dto.response.payment.CreatePaymentResponse;
import com.sma.core.entity.PaymentHistory;
import com.sma.core.entity.Subscription;
import com.sma.core.entity.User;
import com.sma.core.enums.*;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.repository.PaymentRepository;
import com.sma.core.repository.RecruiterRepository;
import com.sma.core.repository.SubscriptionRepository;
import com.sma.core.service.EmailService;
import com.sma.core.service.NotificationService;
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
import org.thymeleaf.context.Context;

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
    final NotificationService notificationService;
    final RecruiterRepository recruiterRepository;
    final EmailService emailService;

    @Override
    public CreatePaymentResponse createQR(Subscription subscription, PaymentMethod method) {
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
            return CreatePaymentResponse.builder()
                    .id(paymentHistory.getId())
                    .qr(String.format(sePayLink, sePayBankAccount, sePayBank, amount,
                            java.net.URLEncoder.encode(description, java.nio.charset.StandardCharsets.UTF_8)))
                    .build();
        }
        return null;
    }

    @Override
    @Transactional
    public Boolean confirm(String authorization, SePayWebhookRequest request) {
        if (!sePayApiKey.equals(authorization)) {
            throw new AppException(ErrorCode.NOT_HAVE_PERMISSION);
        }
        String content = request.getContent();
        if (!content.contains(sePayPrefix)) {
            throw new AppException(ErrorCode.INVALID_SEPAY_CONTENT_FORMAT);
        }
        int startIndex = content.indexOf(sePayPrefix) + sePayPrefix.length();
        Integer id = Integer.parseInt(content.substring(startIndex).trim());

        PaymentHistory paymentHistory = paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        Subscription subscription = paymentHistory.getSubscription();
        String planName = subscription.getPlan().getName();
        LocalDateTime expiredTime = paymentHistory.getCreatedAt().plusMinutes(15);
        if (request.getTransactionDate().isAfter(expiredTime)) {
            paymentHistory.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(paymentHistory);
            sendPaymentNotification(subscription, paymentHistory, false, planName);
            throw new AppException(ErrorCode.PAYMENT_TIME_EXPIRED);
        }
        if (paymentHistory.getPaymentStatus() == PaymentStatus.SUCCESS) {
            return true;
        }
        paymentHistory.setPaidAt(request.getTransactionDate());
        paymentHistory.setPaymentStatus(PaymentStatus.SUCCESS);
        paymentHistory.setTransactionCode(String.valueOf(request.getId()));
//        Subscription subscription = paymentHistory.getSubscription();
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
        sendPaymentNotification(subscription, paymentHistory, true, planName);
        return true;
    }

    @Override
    public PaymentStatus getPaymentStatus(Integer id) {
        PaymentHistory paymentHistory = paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        return paymentHistory.getPaymentStatus();
    }

    private void sendPaymentNotification(Subscription sub, PaymentHistory paymentHistory, boolean isSuccess, String planName) {
        NotificationType type = isSuccess ? NotificationType.PAYMENT_SUCCESS : NotificationType.PAYMENT_FAILURE;
        String title = isSuccess ? "Payment Successful!" : "Payment Action Required!";
        String message;

        if (isSuccess) {
            message = String.format("Thank you! Your payment for the '%s' plan was successful.", planName);
        } else {
            message = String.format("We couldn't process your payment for the '%s' plan because the session expired. Please try again.", planName);
        }

        String displayTransactionCode = isSuccess ? paymentHistory.getTransactionCode() : "#" + paymentHistory.getId();

        User recipient = null;
        if (sub.getCandidate() != null) {
            recipient = sub.getCandidate().getUser();
            notificationService.sendCandidateNotification(recipient, type, title, message, "PAYMENT", paymentHistory.getId());
        } else if (sub.getCompany() != null) {
            recipient = recruiterRepository.findRootUserByCompanyId(sub.getCompany().getId()).orElse(null);
            if (recipient != null) {
                notificationService.sendCandidateNotification(recipient, type, title, message, "PAYMENT", paymentHistory.getId());
            }
        }

        if (recipient != null && recipient.getEmail() != null) {
            sendPaymentEmail(recipient, sub, displayTransactionCode, isSuccess, planName);
        }
    }

    private void sendPaymentEmail(User user, Subscription sub, String transactionCode, boolean isSuccess, String planName) {
        Context context = new Context();
        String displayName = user.getFullName();
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = user.getEmail().split("@")[0];
        }
        context.setVariable("fullName", displayName);
        context.setVariable("isSuccess", isSuccess);
        context.setVariable("planName", planName);
        context.setVariable("transactionCode", transactionCode != null ? transactionCode : "#" + sub.getId());
        context.setVariable("amount", String.format("%,.0f", sub.getPrice()));
        context.setVariable("currency", sub.getPlan().getCurrency());
        context.setVariable("date", java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(java.time.LocalDateTime.now()));
        context.setVariable("paymentMethod", "Bank Transfer / QR Code");

        String subject = isSuccess ? "[SmartRecruit] Payment Successful - " + planName : "[SmartRecruit] Payment Failed Notice";

        emailService.sendEmailWithTemplate(
                user.getEmail(),
                subject,
                "payment",
                context
        );
    }
}
