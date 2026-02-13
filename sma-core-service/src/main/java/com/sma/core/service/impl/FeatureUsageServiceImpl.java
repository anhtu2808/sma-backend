package com.sma.core.service.impl;

import com.sma.core.dto.response.featureusage.FeatureUsageResponse;
import com.sma.core.entity.Feature;
import com.sma.core.entity.Plan;
import com.sma.core.entity.Subscription;
import com.sma.core.entity.UsageLimit;
import com.sma.core.enums.FeatureKey;
import com.sma.core.enums.UsageLimitUnit;
import com.sma.core.enums.UsageType;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.repository.UsageEventRepository;
import com.sma.core.repository.UsageLimitRepository;
import com.sma.core.service.FeatureQuotaRuntimeService;
import com.sma.core.service.FeatureUsageService;
import com.sma.core.dto.model.QuotaOwnerContext;
import com.sma.core.service.quota.impl.SubscriptionQuotaWindowResolver;
import com.sma.core.service.quota.impl.ResumeUploadLimitStateChecker;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class FeatureUsageServiceImpl implements FeatureUsageService {

    FeatureQuotaRuntimeService featureQuotaRuntimeService;
    UsageLimitRepository usageLimitRepository;
    UsageEventRepository usageEventRepository;
    ResumeUploadLimitStateChecker cvUploadLimitStateChecker;
    SubscriptionQuotaWindowResolver subscriptionQuotaWindowResolver;

    @Override
    public List<FeatureUsageResponse> getCurrentUsage() {
        QuotaOwnerContext ownerContext = featureQuotaRuntimeService.resolveOwnerContext();
        LocalDateTime now = LocalDateTime.now();

        List<Subscription> subscriptions = findEligibleSubscriptions(ownerContext, now);
        if (subscriptions.isEmpty()) {
            return List.of();
        }

        List<Integer> planIds = subscriptions.stream()
                .map(Subscription::getPlan)
                .filter(Objects::nonNull)
                .map(Plan::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (planIds.isEmpty()) {
            return List.of();
        }

        List<UsageLimit> usageLimits = usageLimitRepository.findAllByPlanIdInWithFeature(planIds);
        if (usageLimits.isEmpty()) {
            return List.of();
        }

        Map<Integer, List<UsageLimit>> limitsByPlanId = usageLimits.stream()
                .filter(limit -> limit.getPlan() != null && limit.getPlan().getId() != null)
                .collect(Collectors.groupingBy(limit -> limit.getPlan().getId()));

        Map<Integer, List<SubscriptionLimit>> limitsByFeatureId = collectUsageLimitsPerFeature(subscriptions, limitsByPlanId);

        List<FeatureUsageResponse> responses = new ArrayList<>();
        for (Map.Entry<Integer, List<SubscriptionLimit>> entry : limitsByFeatureId.entrySet()) {
            List<SubscriptionLimit> featureLimits = entry.getValue();
            if (featureLimits.isEmpty()) {
                continue;
            }

            Feature feature = featureLimits.get(0).usageLimit().getFeature();
            if (feature == null || !Boolean.TRUE.equals(feature.getIsActive())) {
                continue;
            }

            UsageType usageType = feature.getUsageType();
            if (usageType == null) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }

            UsageLimitUnit limitUnit = null;
            if (usageType != UsageType.BOOLEAN) {
                limitUnit = resolveDistinctLimitUnit(featureLimits);
            }

            FeatureUsageResponse.FeatureUsageResponseBuilder responseBuilder = FeatureUsageResponse.builder()
                    .featureId(feature.getId())
                    .featureKey(feature.getFeatureKey())
                    .featureName(feature.getName())
                    .usageType(usageType)
                    .limitUnit(usageType == UsageType.BOOLEAN ? null : limitUnit);

            if (usageType == UsageType.BOOLEAN) {
                responses.add(responseBuilder.build());
                continue;
            }

            long maxQuota = featureLimits.stream()
                    .map(SubscriptionLimit::usageLimit)
                    .map(UsageLimit::getMaxQuota)
                    .filter(Objects::nonNull)
                    .mapToLong(Integer::longValue)
                    .sum();

            long used = switch (usageType) {
                case EVENT -> sumEventUsage(featureLimits, feature.getId(), limitUnit, now);
                case STATE -> sumStateUsage(ownerContext, feature);
                case BOOLEAN -> 0L;
            };

            long remaining = Math.max(0L, maxQuota - used);

            responses.add(responseBuilder
                    .maxQuota(maxQuota)
                    .used(used)
                    .remaining(remaining)
                    .build());
        }

        return responses.stream()
                .sorted((a, b) -> {
                    String left = a.getFeatureName() == null ? "" : a.getFeatureName();
                    String right = b.getFeatureName() == null ? "" : b.getFeatureName();
                    return left.compareToIgnoreCase(right);
                })
                .toList();
    }

    private List<Subscription> findEligibleSubscriptions(QuotaOwnerContext ownerContext, LocalDateTime now) {
        try {
            return featureQuotaRuntimeService.findEligibleSubscriptions(ownerContext, now);
        } catch (AppException ex) {
            if (ex.getErrorCode() == ErrorCode.FEATURE_NOT_INCLUDED) {
                return List.of();
            }
            throw ex;
        }
    }

    private Map<Integer, List<SubscriptionLimit>> collectUsageLimitsPerFeature(
            List<Subscription> subscriptions,
            Map<Integer, List<UsageLimit>> limitsByPlanId
    ) {
        Map<Integer, List<SubscriptionLimit>> limitsByFeatureId = new HashMap<>();
        for (Subscription subscription : subscriptions) {
            Integer planId = subscription.getPlan() != null ? subscription.getPlan().getId() : null;
            if (planId == null) {
                continue;
            }
            List<UsageLimit> planLimits = limitsByPlanId.getOrDefault(planId, List.of());
            for (UsageLimit usageLimit : planLimits) {
                Feature feature = usageLimit.getFeature();
                if (feature == null || feature.getId() == null || !Boolean.TRUE.equals(feature.getIsActive())) {
                    continue;
                }

                limitsByFeatureId
                        .computeIfAbsent(feature.getId(), ignored -> new ArrayList<>())
                        .add(new SubscriptionLimit(subscription, usageLimit));
            }
        }
        return limitsByFeatureId;
    }

    private UsageLimitUnit resolveDistinctLimitUnit(List<SubscriptionLimit> featureLimits) {
        Set<UsageLimitUnit> units = featureLimits.stream()
                .map(SubscriptionLimit::usageLimit)
                .map(UsageLimit::getLimitUnit)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (units.size() > 1) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
        return units.stream().findFirst().orElse(null);
    }

    private long sumEventUsage(List<SubscriptionLimit> featureLimits,
                               Integer featureId,
                               UsageLimitUnit limitUnit,
                               LocalDateTime now) {
        if (limitUnit == null) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        long used = 0L;
        for (SubscriptionLimit featureLimit : featureLimits) {
            Subscription subscription = featureLimit.subscription();
            Integer subscriptionId = subscription.getId();
            if (subscriptionId == null) {
                continue;
            }

            Long usage = switch (limitUnit) {
                case TOTAL -> usageEventRepository.sumTotal(subscriptionId, featureId);
                case PER_MONTH -> {
                    SubscriptionQuotaWindowResolver.TimeWindow window =
                            subscriptionQuotaWindowResolver.resolveAnchoredMonthlyWindow(subscription, now);
                    yield usageEventRepository.sumInPeriod(
                            subscriptionId,
                            featureId,
                            window.periodStart(),
                            window.periodEnd()
                    );
                }
            };
            used += usage == null ? 0L : usage;
        }

        return used;
    }

    private long sumStateUsage(QuotaOwnerContext ownerContext, Feature feature) {
        FeatureKey featureKey;
        try {
            featureKey = FeatureKey.valueOf(feature.getFeatureKey());
        } catch (Exception ex) {
            throw new AppException(ErrorCode.STATE_CHECKER_NOT_CONFIGURED);
        }

        return switch (featureKey) {
            case CV_UPLOAD_LIMIT -> cvUploadLimitStateChecker.getCurrentUsage(ownerContext, null);
            default -> throw new AppException(ErrorCode.STATE_CHECKER_NOT_CONFIGURED);
        };
    }

    private record SubscriptionLimit(Subscription subscription, UsageLimit usageLimit) {
    }
}
