package com.sma.core.service.impl;

import com.sma.core.entity.*;
import com.sma.core.enums.FeatureKey;
import com.sma.core.enums.Role;
import com.sma.core.enums.UsageEntityType;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.repository.CandidateRepository;
import com.sma.core.repository.FeatureRepository;
import com.sma.core.repository.RecruiterRepository;
import com.sma.core.repository.SubscriptionRepository;
import com.sma.core.repository.UsageEventRepository;
import com.sma.core.repository.UsageLimitRepository;
import com.sma.core.service.FeatureService;
import com.sma.core.service.QuotaService;
import com.sma.core.service.SubscriptionService;
import com.sma.core.service.quota.EventUsageCalculator;
import com.sma.core.service.quota.StateQuotaChecker;
import com.sma.core.service.quota.impl.ResumeUploadLimitStateChecker;
import com.sma.core.dto.model.QuotaOwnerContext;
import com.sma.core.utils.JwtTokenProvider;
import org.springframework.context.ApplicationContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuotaServiceImpl implements QuotaService {

    FeatureRepository featureRepository;
    RecruiterRepository recruiterRepository;
    CandidateRepository candidateRepository;
    SubscriptionRepository subscriptionRepository;
    UsageEventRepository usageEventRepository;
    UsageLimitRepository usageLimitRepository;

    FeatureService featureService;
    SubscriptionService subscriptionService;
    EventUsageCalculator eventUsageCalculator;

    ApplicationContext applicationContext;

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
            return QuotaOwnerContext.builder().role(role).candidateId(candidateId).build();
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
    public EventReservation reserveEventQuota(List<Subscription> subscriptions, Integer featureId, int amount, LocalDateTime now) {
        List<Integer> planIds = extractDistinctPlanIds(subscriptions);
        if (planIds.isEmpty()) {
            throw new AppException(ErrorCode.FEATURE_NOT_INCLUDED);
        }

        Map<Integer, UsageLimit> limitsByPlanId = usageLimitRepository.findAllByPlanIdInAndFeatureId(planIds, featureId)
                                                                      .stream()
                                                                      .collect(Collectors.toMap(ul -> ul.getPlan()
                                                                                                        .getId(), Function.identity(), (a, b) -> a));
        if (limitsByPlanId.isEmpty()) {
            throw new AppException(ErrorCode.FEATURE_NOT_INCLUDED);
        }

        for (Subscription subscription : subscriptions) {
            Integer planId = subscription.getPlan() != null ? subscription.getPlan().getId() : null;
            if (planId == null) {
                continue;
            }
            UsageLimit usageLimit = limitsByPlanId.get(planId);
            if (usageLimit == null || usageLimit.getMaxQuota() == null) {
                continue;
            }

            Subscription lockedSubscription = subscriptionRepository.lockById(subscription.getId())
                                                                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
            Integer lockedPlanId = lockedSubscription.getPlan() != null ? lockedSubscription.getPlan().getId() : null;
            if (lockedPlanId == null) {
                continue;
            }
            if (!lockedPlanId.equals(planId)) {
                usageLimit = limitsByPlanId.get(lockedPlanId);
                if (usageLimit == null || usageLimit.getMaxQuota() == null) {
                    continue;
                }
            }

            // Use EventUsageCalculator to calculate used amount - eliminates duplication
            long usedAmount = eventUsageCalculator.calculate(List.of(lockedSubscription), featureId, usageLimit.getLimitUnit(), null, null);

            if (usedAmount + amount <= usageLimit.getMaxQuota()) {
                return new EventReservation(lockedSubscription.getId(), featureId, amount);
            }
        }

        Feature feature = featureRepository.findById(featureId)
                                           .orElseThrow(() -> new AppException(ErrorCode.FEATURE_NOT_FOUND));
        throw buildQuotaExceeded(feature);
    }

    @Override
    public void commitReservation(EventReservation reservation) {
        commitReservation(reservation, null, null);
    }

    @Override
    public void commitReservation(EventReservation reservation, UsageEntityType entityType, Integer entityId) {
        UsageEvent usageEvent = UsageEvent.builder()
                .subscription(subscriptionRepository.getReferenceById(reservation.subscriptionId()))
                .feature(featureRepository.getReferenceById(reservation.featureId()))
                .amount(reservation.amount())
                .build();

        if (entityType != null && entityId != null) {
            UsageEventContext context = UsageEventContext.builder()
                    .usageEvent(usageEvent)
                    .entityType(entityType)
                    .entityId(entityId)
                    .build();
            usageEvent.getContexts().add(context);
        }

        usageEventRepository.save(usageEvent);
    }

    @Override
    public void validateBooleanQuota(FeatureKey featureKey) {
        LocalDateTime now = LocalDateTime.now();
        QuotaOwnerContext ownerContext = resolveOwnerContext();
        List<Subscription> subscriptions = subscriptionService.findEligibleSubscriptions(ownerContext, now);

        Feature feature = featureService.getActiveFeature(featureKey);
        if (!hasBooleanEntitlement(subscriptions, feature.getId())) {
            throw new AppException(ErrorCode.FEATURE_NOT_INCLUDED);
        }
    }

    @Override
    public void validateStateQuota(FeatureKey featureKey, Object input) {
        LocalDateTime now = LocalDateTime.now();
        QuotaOwnerContext ownerContext = resolveOwnerContext();
        List<Subscription> subscriptions = subscriptionService.findEligibleSubscriptions(ownerContext, now);

        Feature feature = featureService.getActiveFeature(featureKey);
        long limit = getStateEffectiveLimit(subscriptions, feature.getId());

        // Get the state checker from application context
        StateQuotaChecker checker = getStateChecker(featureKey);
        if (checker != null) {
            long currentUsage = checker.getCurrentUsage(ownerContext, input);
            if (currentUsage >= limit) {
                throw new AppException(ErrorCode.FEATURE_QUOTA_EXCEEDED, String.format("Feature quota exceeded for '%s': limit=%d, current=%d", featureKey, limit, currentUsage));
            }
        }
    }

    @Override
    public void consumeEventQuota(FeatureKey featureKey, int amount, UsageEntityType entityType, Integer entityId) {
        LocalDateTime now = LocalDateTime.now();
        QuotaOwnerContext ownerContext = resolveOwnerContext();
        List<Subscription> subscriptions = subscriptionService.findEligibleSubscriptions(ownerContext, now);

        Feature feature = featureService.getActiveFeature(featureKey);

        EventReservation reservation = reserveEventQuota(subscriptions, feature.getId(), amount, now);

        commitReservation(reservation, entityType, entityId);
    }

    private StateQuotaChecker getStateChecker(FeatureKey featureKey) {
        return switch (featureKey) {
            case CV_UPLOAD_LIMIT -> getBean(ResumeUploadLimitStateChecker.class);
            default -> null;
        };
    }

    @SuppressWarnings("unchecked")
    private <T> T getBean(Class<T> beanClass) {
        try {
            return applicationContext.getBean(beanClass);
        } catch (Exception e) {
            return null;
        }
    }

    private AppException buildQuotaExceeded(Feature feature) {
        String featureName = feature != null && feature.getName() != null ? feature.getName() : "this feature";

        String message = "Sorry, you have reached the usage limit for " + featureName + ".";
        return new AppException(ErrorCode.FEATURE_QUOTA_EXCEEDED, message);
    }

    private List<Integer> extractDistinctPlanIds(List<Subscription> subscriptions) {
        if (subscriptions == null || subscriptions.isEmpty()) {
            return Collections.emptyList();
        }
        return subscriptions.stream()
                            .map(Subscription::getPlan)
                            .filter(Objects::nonNull)
                            .map(Plan::getId)
                            .distinct()
                            .toList();
    }
}
