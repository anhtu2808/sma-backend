package com.sma.core.service.impl;

import com.sma.core.dto.model.QuotaAggregate;
import com.sma.core.dto.model.QuotaOwnerContext;
import com.sma.core.dto.response.featureusage.FeatureUsageResponse;
import com.sma.core.dto.response.quota.QuotaConsumptionResponse;
import com.sma.core.entity.Feature;
import com.sma.core.entity.Subscription;
import com.sma.core.entity.UsageLimit;
import com.sma.core.enums.FeatureKey;
import com.sma.core.enums.Role;
import com.sma.core.enums.SubscriptionStatus;
import com.sma.core.enums.UsageEntityType;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.repository.UsageLimitRepository;
import com.sma.core.service.FeatureService;
import com.sma.core.service.SubscriptionService;
import com.sma.core.service.QuotaService;
import com.sma.core.service.UsageService;
import com.sma.core.service.quota.EventUsageCalculator;
import com.sma.core.service.quota.QuotaAggregationEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsageServiceImpl implements UsageService {

    private final QuotaService quotaService;
    private final SubscriptionService subscriptionService;
    private final UsageLimitRepository usageLimitRepository;
    private final QuotaAggregationEngine quotaEngine;
    private final EventUsageCalculator eventUsageCalculator;
    private final FeatureService featureService;

    @Override
    public List<FeatureUsageResponse> getCurrentUsage() {

        QuotaOwnerContext ownerContext = quotaService.resolveOwnerContext();
        List<Subscription> subscriptions = subscriptionService.findEligibleSubscriptions(ownerContext, LocalDateTime.now());

        if (subscriptions.isEmpty()) return List.of();

        var planIds = subscriptions.stream()
                                   .map(s -> s.getPlan() != null ? s.getPlan().getId() : null)
                                   .filter(Objects::nonNull)
                                   .distinct()
                                   .toList();

        if (planIds.isEmpty()) return List.of();

        var usageLimits = usageLimitRepository.findAllByPlanIdInWithFeature(planIds);

        Map<Integer, List<UsageLimit>> limitsByFeature =
                usageLimits.stream()
                           .filter(l -> l.getFeature() != null)
                           .collect(Collectors.groupingBy(l -> l.getFeature().getId()));

        List<FeatureUsageResponse> responses = new ArrayList<>();

        for (var entry : limitsByFeature.entrySet()) {

            Feature feature = entry.getValue().get(0).getFeature();

            QuotaAggregate aggregate = quotaEngine.aggregate(
                    feature,
                    subscriptions,
                    entry.getValue(),
                    ownerContext
            );

            responses.add(
                    FeatureUsageResponse.builder()
                            .featureId(aggregate.getFeatureId())
                            .featureKey(aggregate.getFeatureKey())
                            .featureName(aggregate.getFeatureName())
                            .usageType(aggregate.getUsageType())
                            .limitUnit(aggregate.getLimitUnit())
                            .maxQuota(aggregate.getMaxQuota())
                            .used(aggregate.getUsed())
                            .remaining(aggregate.getRemaining())
                            .build()
            );
        }

        return responses;
    }
}