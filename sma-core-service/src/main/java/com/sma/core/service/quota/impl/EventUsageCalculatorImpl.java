package com.sma.core.service.quota.impl;

import com.sma.core.entity.Subscription;
import com.sma.core.enums.EventSource;
import com.sma.core.enums.UsageEventStatus;
import com.sma.core.enums.UsageLimitUnit;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.repository.UsageEventRepository;
import com.sma.core.service.quota.EventUsageCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EventUsageCalculatorImpl implements EventUsageCalculator {

    private final UsageEventRepository usageEventRepository;
    private final SubscriptionQuotaWindowResolver windowResolver;

    public long calculate(
            List<Subscription> subscriptions,
            Integer featureId,
            UsageLimitUnit unit,
            EventSource eventSource,
            Integer sourceId
    ) {
        LocalDateTime now = LocalDateTime.now();
        long total = 0L;

        for (Subscription subscription : subscriptions) {

            if (subscription.getId() == null) continue;

            Long usage;

            switch (unit) {

                case TOTAL -> {
                    usage = eventSource == null && sourceId == null
                            ? usageEventRepository.sumTotal(subscription.getId(), featureId, UsageEventStatus.SUCCESS)
                            : usageEventRepository.sumTotalByContext(
                            subscription.getId(),
                            featureId,
                            eventSource,
                            sourceId,
                            UsageEventStatus.SUCCESS
                    );
                }

                case PER_MONTH -> {
                    var window = windowResolver.resolveAnchoredMonthlyWindow(subscription, now);

                    usage = eventSource == null && sourceId == null
                            ? usageEventRepository.sumInPeriod(
                            subscription.getId(),
                            featureId,
                            window.periodStart(),
                            window.periodEnd(),
                            UsageEventStatus.SUCCESS
                    )
                            : usageEventRepository.sumInPeriodByContext(
                            subscription.getId(),
                            featureId,
                            window.periodStart(),
                            window.periodEnd(),
                            eventSource,
                            sourceId,
                            UsageEventStatus.SUCCESS
                    );
                }

                default -> throw new AppException(ErrorCode.BAD_REQUEST, "Unsupported unit");
            }

            total += usage == null ? 0L : usage;
        }

        return total;
    }
}
