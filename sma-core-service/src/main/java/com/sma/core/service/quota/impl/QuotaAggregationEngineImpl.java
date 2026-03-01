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
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QuotaAggregationEngineImpl implements QuotaAggregationEngine {

    private final EventUsageCalculatorImpl eventUsageCalculator;
    private final ResumeUploadLimitStateChecker resumeUploadLimitStateChecker;

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

        return QuotaAggregate.builder()
                .featureId(feature.getId())
                .featureKey(feature.getFeatureKey())
                .featureName(feature.getName())
                .usageType(usageType)
                .limitUnit(unit)
                .maxQuota(maxQuota)
                .used(used)
                .remaining(Math.max(0L, maxQuota - used))
                .build();
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
            throw new AppException(ErrorCode.STATE_CHECKER_NOT_CONFIGURED);
        }

        return switch (key) {
            case CV_UPLOAD_LIMIT -> resumeUploadLimitStateChecker.getCurrentUsage(context, null);
            default -> throw new AppException(ErrorCode.STATE_CHECKER_NOT_CONFIGURED);
        };
    }
}