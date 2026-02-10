package com.sma.core.service.impl;

import com.sma.core.dto.request.subscription.CreateSubscriptionRequest;
import com.sma.core.entity.*;
import com.sma.core.enums.PaymentMethod;
import com.sma.core.enums.Role;
import com.sma.core.enums.SubscriptionStatus;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.repository.*;
import com.sma.core.service.PaymentService;
import com.sma.core.service.SubscriptionService;
import com.sma.core.utils.JwtTokenProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    final SubscriptionRepository subscriptionRepository;
    final PlanPriceRepository planPriceRepository;
    final CandidateRepository candidateRepository;
    final RecruiterRepository recruiterRepository;
    final PaymentService paymentService;

    @Override
    public String createSubscription(CreateSubscriptionRequest request) {
        PlanPrice planPrice = planPriceRepository.findById(request.getPlanPriceId())
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_PRICE_NOT_FOUND));
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = switch (planPrice.getUnit()) {
            case MONTH -> now.plusMonths(planPrice.getDuration());
            case YEAR -> now.plusYears(planPrice.getDuration());
        };

        Subscription subscription = Subscription.builder()
                .startDate(now)
                .endDate(endDate)
                .price(planPrice.getSalePrice())
                .plan(planPrice.getPlan())
                .status(SubscriptionStatus.PENDING_PAYMENT)
                .build();
        if (Objects.equals(JwtTokenProvider.getCurrentRole(), Role.CANDIDATE)){
            Candidate candidate = candidateRepository
                    .findById(JwtTokenProvider.getCurrentCandidateId())
                    .orElseThrow(() -> new AppException(ErrorCode.CANDIDATE_NOT_EXISTED));
            subscription.setCandidate(candidate);
        } else {
            Company company = recruiterRepository
                    .findById(JwtTokenProvider.getCurrentRecruiterId())
                    .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED))
                    .getCompany();
            subscription.setCompany(company);
        }
        subscriptionRepository.save(subscription);
        return paymentService.createQR(subscription, PaymentMethod.SEPAY);
    }
}
