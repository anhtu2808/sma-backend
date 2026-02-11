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
        Subscription subscription = buildSubscription(request.getPlanPriceId());
        if (Objects.equals(JwtTokenProvider.getCurrentRole(), Role.CANDIDATE)){
            subscription = bindCandidate(subscription, JwtTokenProvider.getCurrentCandidateId());
        } else {
            subscription = bindCompany(subscription, JwtTokenProvider.getCurrentRecruiterId());
        }
        subscriptionRepository.save(subscription);
        return paymentService.createQR(subscription, PaymentMethod.SEPAY);
    }

    @Override
    public String createSubscription(Integer targetId, CreateSubscriptionRequest request, Role role) {
        Subscription subscription = buildSubscription(request.getPlanPriceId());
        if (role.equals(Role.CANDIDATE)){
            subscription = bindCandidate(subscription, targetId);
        } else {
            subscription = bindCompany(subscription, targetId);
        }
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(subscription);
        return "";
    }

    Subscription buildSubscription(Integer planPriceId) {
        PlanPrice planPrice = planPriceRepository.findById(planPriceId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_PRICE_NOT_FOUND));
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = switch (planPrice.getUnit()) {
            case MONTH -> now.plusMonths(planPrice.getDuration());
            case YEAR -> now.plusYears(planPrice.getDuration());
        };

        return Subscription.builder()
                .startDate(now)
                .endDate(endDate)
                .price(planPrice.getSalePrice())
                .plan(planPrice.getPlan())
                .status(SubscriptionStatus.PENDING_PAYMENT)
                .build();
    }

    Subscription bindCandidate(Subscription subscription, Integer candidateId){
        Candidate candidate = candidateRepository
                .findById(candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.CANDIDATE_NOT_EXISTED));
        subscription.setCandidate(candidate);
        return subscription;
    }

    Subscription bindCompany(Subscription subscription, Integer recruiterId){
        Company company = recruiterRepository
                .findById(recruiterId)
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED))
                .getCompany();
        subscription.setCompany(company);
        return subscription;
    }
}
