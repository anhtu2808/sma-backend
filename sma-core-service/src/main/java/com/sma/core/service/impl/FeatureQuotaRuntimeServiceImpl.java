package com.sma.core.service.impl;

import com.sma.core.entity.Feature;
import com.sma.core.entity.Recruiter;
import com.sma.core.entity.Subscription;
import com.sma.core.entity.UsageEvent;
import com.sma.core.entity.UsageLimit;
import com.sma.core.enums.FeatureKey;
import com.sma.core.enums.PlanType;
import com.sma.core.enums.Role;
import com.sma.core.enums.SubscriptionStatus;
import com.sma.core.enums.UsageLimitUnit;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.repository.CandidateRepository;
import com.sma.core.repository.FeatureRepository;
import com.sma.core.repository.RecruiterRepository;
import com.sma.core.repository.SubscriptionRepository;
import com.sma.core.repository.UsageEventRepository;
import com.sma.core.repository.UsageLimitRepository;
import com.sma.core.service.FeatureQuotaRuntimeService;
import com.sma.core.service.quota.QuotaOwnerContext;
import com.sma.core.utils.JwtTokenProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeatureQuotaRuntimeServiceImpl implements FeatureQuotaRuntimeService {

    FeatureRepository featureRepository;
    RecruiterRepository recruiterRepository;
    CandidateRepository candidateRepository;
    SubscriptionRepository subscriptionRepository;
    UsageLimitRepository usageLimitRepository;
    UsageEventRepository usageEventRepository;

    @Override
    public QuotaOwnerContext resolveOwnerContext() {
        Role role = JwtTokenProvider.getCurrentRole();
        if (role == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (role == Role.CANDIDATE) {
            Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
            if (!candidateRepository.existsById(candidateId)) {
                throw new AppException(ErrorCode.CANDIDATE_NOT_EXISTED);
            }
            return QuotaOwnerContext.builder()
                    .role(role)
                    .candidateId(candidateId)
                    .build();
        }

        if (role == Role.RECRUITER) {
            Integer recruiterId = JwtTokenProvider.getCurrentRecruiterId();
            Recruiter recruiter = recruiterRepository.findById(recruiterId)
                    .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED));
            return QuotaOwnerContext.builder()
                    .role(role)
                    .recruiterId(recruiterId)
                    .companyId(recruiter.getCompany().getId())
                    .build();
        }

        throw new AppException(ErrorCode.NOT_HAVE_PERMISSION);
    }

    @Override
    public Feature resolveActiveFeature(FeatureKey featureKey) {
        return featureRepository.findByFeatureKeyAndIsActiveTrue(featureKey.name())
                .orElseThrow(() -> new AppException(ErrorCode.FEATURE_DISABLED));
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
    public boolean hasBooleanEntitlement(List<Subscription> subscriptions, Integer featureId) {
        List<Integer> planIds = extractDistinctPlanIds(subscriptions);
        if (planIds.isEmpty()) {
            return false;
        }
        return usageLimitRepository.existsByPlanIdInAndFeatureId(planIds, featureId);
    }

    @Override
    public long getStateEffectiveLimit(List<Subscription> subscriptions, Integer featureId) {
        List<Integer> planIds = extractDistinctPlanIds(subscriptions);
        if (planIds.isEmpty()) {
            return 0;
        }
        return usageLimitRepository.sumMaxQuotaByPlanIdInAndFeatureId(planIds, featureId);
    }

    @Override
    public EventReservation selectEventReservation(List<Subscription> subscriptions,
                                                   Integer featureId,
                                                   int amount,
                                                   LocalDateTime now) {
        List<Integer> planIds = extractDistinctPlanIds(subscriptions);
        if (planIds.isEmpty()) {
            throw new AppException(ErrorCode.FEATURE_NOT_INCLUDED);
        }

        Map<Integer, UsageLimit> limitByPlanId = usageLimitRepository.findAllByPlanIdInAndFeatureId(planIds, featureId)
                .stream()
                .collect(Collectors.toMap(ul -> ul.getPlan().getId(), Function.identity(), (a, b) -> a));
        if (limitByPlanId.isEmpty()) {
            throw new AppException(ErrorCode.FEATURE_NOT_INCLUDED);
        }

        for (Subscription subscription : subscriptions) {
            Subscription lockedSubscription = subscriptionRepository.lockById(subscription.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
            UsageLimit usageLimit = limitByPlanId.get(lockedSubscription.getPlan().getId());
            if (usageLimit == null || usageLimit.getMaxQuota() == null) {
                continue;
            }

            long used = switch (usageLimit.getLimitUnit()) {
                case TOTAL -> usageEventRepository.sumTotal(lockedSubscription.getId(), featureId);
                case PER_MONTH -> {
                    TimeWindow window = resolveAnchoredMonthlyWindow(lockedSubscription, now);
                    yield usageEventRepository.sumInPeriod(
                            lockedSubscription.getId(),
                            featureId,
                            window.periodStart(),
                            window.periodEnd()
                    );
                }
            };

            if (used + amount <= usageLimit.getMaxQuota()) {
                return new EventReservation(lockedSubscription.getId(), featureId, amount);
            }
        }

        throw new AppException(ErrorCode.FEATURE_QUOTA_EXCEEDED);
    }

    @Override
    public void saveUsageEvent(EventReservation reservation) {
        UsageEvent usageEvent = UsageEvent.builder()
                .subscription(subscriptionRepository.getReferenceById(reservation.subscriptionId()))
                .feature(featureRepository.getReferenceById(reservation.featureId()))
                .amount(reservation.amount())
                .build();
        usageEventRepository.save(usageEvent);
    }

    private List<Integer> extractDistinctPlanIds(List<Subscription> subscriptions) {
        if (subscriptions == null || subscriptions.isEmpty()) {
            return Collections.emptyList();
        }
        return subscriptions.stream()
                .map(Subscription::getPlan)
                .filter(Objects::nonNull)
                .map(plan -> plan.getId())
                .distinct()
                .toList();
    }

    private TimeWindow resolveAnchoredMonthlyWindow(Subscription subscription, LocalDateTime now) {
        LocalDateTime startDate = subscription.getStartDate();
        if (startDate == null) {
            throw new AppException(ErrorCode.FEATURE_QUOTA_EXCEEDED);
        }

        LocalDate anchorDate = startDate.toLocalDate();
        LocalDate today = now.toLocalDate();

        int monthsBetween = (today.getYear() - anchorDate.getYear()) * 12
                + (today.getMonthValue() - anchorDate.getMonthValue());
        LocalDate periodAnchorDate = anchorDate.plusMonths(monthsBetween);
        if (periodAnchorDate.isAfter(today)) {
            periodAnchorDate = anchorDate.plusMonths(monthsBetween - 1L);
        }

        LocalDateTime periodStart = periodAnchorDate.atStartOfDay();
        LocalDateTime periodEnd = periodStart.plusMonths(1);
        return new TimeWindow(periodStart, periodEnd);
    }

    private boolean isAddonPlan(Subscription subscription) {
        if (subscription == null || subscription.getPlan() == null || subscription.getPlan().getPlanType() == null) {
            return false;
        }
        PlanType planType = subscription.getPlan().getPlanType();
        return planType == PlanType.ADDONS_FEATURE || planType == PlanType.ADDONS_QUOTA;
    }

    private record TimeWindow(LocalDateTime periodStart, LocalDateTime periodEnd) {
    }
}
