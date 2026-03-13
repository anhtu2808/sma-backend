package com.sma.core.service.quota.impl;

import com.sma.core.dto.model.QuotaAggregate;
import com.sma.core.dto.model.QuotaOwnerContext;
import com.sma.core.entity.Feature;
import com.sma.core.entity.Subscription;
import com.sma.core.entity.UsageLimit;
import com.sma.core.enums.*;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.service.quota.QuotaAggregationEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuotaAggregationEngineImpl implements QuotaAggregationEngine {

    private final EventUsageCalculatorImpl eventUsageCalculator;
    private final ResumeUploadLimitStateChecker resumeUploadLimitStateChecker;
    private final TeamMemberLimitStateChecker teamMemberLimitStateChecker;
    private final SubscriptionQuotaWindowResolver windowResolver;
    private final Clock clock;

    public QuotaAggregate aggregate(
            Feature feature,
            List<Subscription> subscriptions,
            List<UsageLimit> limits,
            QuotaOwnerContext ownerContext
    ) {

        UsageType usageType = feature.getUsageType();

        if (usageType == UsageType.BOOLEAN) {
            return QuotaAggregate.booleanFeature(feature);
        }

        UsageLimitUnit unit = resolveUnit(limits);

        long maxQuota = limits.stream()
                              .map(UsageLimit::getMaxQuota)
                              .filter(Objects::nonNull)
                              .mapToLong(Integer::longValue)
                              .sum();

        long used = switch (usageType) {

            case EVENT -> eventUsageCalculator.calculate(
                    subscriptions,
                    feature.getId(),
                    unit,
                    null,
                    null
            );

            case STATE -> calculateStateUsage(feature, ownerContext);

            default -> 0L;
        };

        LocalDateTime renewDate = calculateRenewDate(unit, subscriptions);

        return QuotaAggregate.builder()
                .featureId(feature.getId())
                .featureKey(feature.getFeatureKey())
                .featureName(feature.getName())
                .usageType(usageType)
                .limitUnit(unit)
                .maxQuota(maxQuota)
                .used(used)
                .remaining(Math.max(0L, maxQuota - used))
                .renewDate(renewDate)
                .build();
    }

    private LocalDateTime calculateRenewDate(UsageLimitUnit unit, List<Subscription> subscriptions) {
        if (subscriptions == null || subscriptions.isEmpty()) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now(clock);

        return switch (unit) {
            case PER_MONTH -> {
                // Lấy subscription đầu tiên để tính window (tất cả subscriptions cùng owner)
                Subscription subscription = subscriptions.get(0);
                var window = windowResolver.resolveAnchoredMonthlyWindow(subscription, now);
                yield window.periodEnd();
            }
            case TOTAL -> {
                // Với TOTAL, renew date là endDate sớm nhất của các subscriptions
                yield subscriptions.stream()
                        .map(Subscription::getEndDate)
                        .filter(Objects::nonNull)
                        .min(Comparator.naturalOrder())
                        .orElse(null);
            }
        };
    }

    private UsageLimitUnit resolveUnit(List<UsageLimit> limits) {
        Set<UsageLimitUnit> units = limits.stream()
                                          .map(UsageLimit::getLimitUnit)
                                          .filter(Objects::nonNull)
                                          .collect(Collectors.toSet());

        if (units.size() != 1) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        return units.iterator().next();
    }

    private long calculateStateUsage(Feature feature, QuotaOwnerContext context) {

        FeatureKey key;

        try {
            key = FeatureKey.valueOf(feature.getFeatureKey());
        } catch (Exception ex) {
            log.warn("State checker not configured for featureKey={}", feature.getFeatureKey());
            return 0L;
        }

        return switch (key) {
            case CV_UPLOAD_LIMIT -> resumeUploadLimitStateChecker.getCurrentUsage(context, null);
            case TEAM_MEMBER_LIMIT -> teamMemberLimitStateChecker.getCurrentUsage(context, null);
            default -> {
                log.warn("State checker not configured for featureKey={}", key);
                yield 0L;
            }
        };
    }
}
