package com.sma.core.service.quota.impl;

import com.sma.core.entity.Subscription;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class SubscriptionQuotaWindowResolver {

    public TimeWindow resolveAnchoredMonthlyWindow(Subscription subscription, LocalDateTime now) {
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

    public record TimeWindow(LocalDateTime periodStart, LocalDateTime periodEnd) {
    }
}
