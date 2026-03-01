package com.sma.core.service.impl;

import com.sma.core.dto.model.QuotaOwnerContext;
import com.sma.core.dto.request.subscription.CreateSubscriptionRequest;
import com.sma.core.entity.*;
import com.sma.core.enums.PaymentMethod;
import com.sma.core.enums.PlanDurationUnit;
import com.sma.core.enums.PlanTarget;
import com.sma.core.enums.PlanType;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    static final LocalDateTime LIFETIME_END_DATE = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

    final SubscriptionRepository subscriptionRepository;
    final PlanPriceRepository planPriceRepository;
    final PlanRepository planRepository;
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
        if (subscription.getPlan() != null && subscription.getPlan().getPlanType() == PlanType.MAIN) {
            expireActiveMainSubscriptions(subscription, LocalDateTime.now(), null);
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
            case LIFETIME -> LIFETIME_END_DATE;
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

    @Override
    public void assignDefaultPlanForCandidate(Integer candidateId) {
        if (candidateId == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        boolean hasActiveMain = subscriptionRepository
                .existsByCandidateIdAndStatusAndPlan_PlanTypeAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        candidateId,
                        SubscriptionStatus.ACTIVE,
                        PlanType.MAIN,
                        now,
                        now
                );
        if (hasActiveMain) {
            return;
        }
        Plan defaultPlan = planRepository
                .findFirstByPlanTargetAndPlanTypeAndIsDefaultTrueAndIsActiveTrue(PlanTarget.CANDIDATE, PlanType.MAIN)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));
        PlanPrice planPrice = planPriceRepository
                .findFirstByPlanIdAndUnitAndIsActiveTrue(defaultPlan.getId(), PlanDurationUnit.LIFETIME)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_PRICE_NOT_FOUND));

        Subscription subscription = buildSubscription(planPrice.getId());
        subscription = bindCandidate(subscription, candidateId);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(subscription);
    }

    @Override
    public List<Subscription> findEligibleSubscriptions(QuotaOwnerContext ownerContext, LocalDateTime now) {
        List<Subscription> subscriptions;
        if (ownerContext.getRole() == Role.CANDIDATE) {
            subscriptions = subscriptionRepository.findEligibleByCandidateId(
                    ownerContext.getCandidateId(),
                    SubscriptionStatus.ACTIVE,
                    now
            );
        } else if (ownerContext.getRole() == Role.RECRUITER) {
            subscriptions = subscriptionRepository.findEligibleByCompanyId(
                    ownerContext.getCompanyId(),
                    SubscriptionStatus.ACTIVE,
                    now
            );
        } else {
            throw new AppException(ErrorCode.NOT_HAVE_PERMISSION);
        }

        if (subscriptions == null || subscriptions.isEmpty()) {
            throw new AppException(ErrorCode.FEATURE_NOT_INCLUDED);
        }
        return subscriptions.stream()
                            .sorted(Comparator
                                    .comparingInt((Subscription s) -> isAddonPlan(s) ? 0 : 1)
                                    .thenComparing(Subscription::getPurchasedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                                    .thenComparing(Subscription::getId))
                            .toList();
    }

    @Override
    public List<Subscription> findAllSubscriptions(QuotaOwnerContext ownerContext) {
        List<Subscription> subscriptions;
        if (ownerContext.getRole() == Role.CANDIDATE) {
            subscriptions = subscriptionRepository.findAllByCandidate_Id(ownerContext.getCandidateId());
        } else if (ownerContext.getRole() == Role.RECRUITER) {
            subscriptions = subscriptionRepository.findAllByCompany_Id(ownerContext.getCompanyId());
        } else {
            throw new AppException(ErrorCode.NOT_HAVE_PERMISSION);
        }

        if (subscriptions == null || subscriptions.isEmpty()) {
            return new ArrayList<>();
        }
        return subscriptions.stream()
                            .sorted(Comparator
                                    .comparingInt((Subscription s) -> isAddonPlan(s) ? 0 : 1)
                                    .thenComparing(Subscription::getPurchasedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                                    .thenComparing(Subscription::getId))
                            .toList();
    }


    private boolean isAddonPlan(Subscription subscription) {
        if (subscription == null || subscription.getPlan() == null || subscription.getPlan().getPlanType() == null) {
            return false;
        }
        PlanType planType = subscription.getPlan().getPlanType();
        return planType == PlanType.ADDONS_FEATURE || planType == PlanType.ADDONS_QUOTA;
    }

    private void expireActiveMainSubscriptions(Subscription subscription, LocalDateTime now, Integer excludeId) {
        if (subscription == null || subscription.getPlan() == null) {
            return;
        }
        if (subscription.getPlan().getPlanType() != PlanType.MAIN) {
            return;
        }
        if (subscription.getCandidate() != null) {
            subscriptionRepository.expireActiveMainByCandidateId(
                    subscription.getCandidate().getId(),
                    SubscriptionStatus.ACTIVE,
                    SubscriptionStatus.EXPIRED,
                    PlanType.MAIN,
                    now,
                    now,
                    excludeId
            );
            return;
        }
        if (subscription.getCompany() != null) {
            subscriptionRepository.expireActiveMainByCompanyId(
                    subscription.getCompany().getId(),
                    SubscriptionStatus.ACTIVE,
                    SubscriptionStatus.EXPIRED,
                    PlanType.MAIN,
                    now,
                    now,
                    excludeId
            );
        }
    }
}
