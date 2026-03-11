package com.sma.core.service.impl;

import com.sma.core.dto.model.QuotaOwnerContext;
import com.sma.core.enums.Role;
import com.sma.core.dto.response.featureusage.FeatureUsageResponse;
import com.sma.core.entity.*;
import com.sma.core.enums.UsageLimitUnit;
import com.sma.core.enums.UsageType;
import com.sma.core.repository.UsageEventRepository;
import com.sma.core.repository.UsageLimitRepository;
import com.sma.core.service.FeatureService;
import com.sma.core.service.QuotaService;
import com.sma.core.service.SubscriptionService;
import com.sma.core.service.quota.QuotaAggregationEngine;
import com.sma.core.service.quota.impl.EventUsageCalculatorImpl;
import com.sma.core.service.quota.impl.QuotaAggregationEngineImpl;
import com.sma.core.service.quota.impl.ResumeUploadLimitStateChecker;
import com.sma.core.service.quota.impl.SubscriptionQuotaWindowResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@DisplayName("UsageServiceImpl Tests")
@ExtendWith(MockitoExtension.class)
class UsageServiceImplTest {

    @Mock
    private QuotaService quotaService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private UsageEventRepository usageEventRepository;

    @Mock
    private UsageLimitRepository usageLimitRepository;

    @Mock
    private FeatureService featureService;

    @Mock
    private ResumeUploadLimitStateChecker resumeUploadLimitStateChecker;

    private UsageServiceImpl usageService;
    private SubscriptionQuotaWindowResolver windowResolver;
    private EventUsageCalculatorImpl eventUsageCalculator;
    private QuotaAggregationEngine quotaEngine;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        windowResolver = new SubscriptionQuotaWindowResolver();
        eventUsageCalculator = new EventUsageCalculatorImpl(usageEventRepository, windowResolver);
        fixedClock = Clock.fixed(Instant.parse("2026-03-15T10:00:00Z"), ZoneId.of("UTC"));
        quotaEngine = new QuotaAggregationEngineImpl(
                eventUsageCalculator,
                resumeUploadLimitStateChecker,
                windowResolver,
                fixedClock
        );

        usageService = new UsageServiceImpl(
                quotaService,
                subscriptionService,
                usageEventRepository,
                usageLimitRepository,
                quotaEngine,
                eventUsageCalculator,
                featureService,
                null  // UsageEventMapper không cần cho getCurrentUsage
        );
    }

    private Subscription createSubscription(Integer id, LocalDateTime startDate, LocalDateTime endDate) {
        return Subscription.builder()
                .id(id)
                .startDate(startDate)
                .endDate(endDate)
                .plan(Plan.builder().id(1).build())
                .company(Company.builder().id(1).build())
                .build();
    }

    private Feature createFeature(Integer id, String key, String name, UsageType usageType) {
        return Feature.builder()
                .id(id)
                .featureKey(key)
                .name(name)
                .usageType(usageType)
                .build();
    }

    private UsageLimit createUsageLimit(Feature feature, Integer maxQuota, UsageLimitUnit unit) {
        return UsageLimit.builder()
                .feature(feature)
                .maxQuota(maxQuota)
                .limitUnit(unit)
                .build();
    }

    @Nested
    @DisplayName("getCurrentUsage - Renew Date Integration Tests")
    class GetCurrentUsageRenewDateTests {

        @Test
        @DisplayName("Should return empty list when no subscriptions")
        void shouldReturnEmptyListWhenNoSubscriptions() {
            // Given
            QuotaOwnerContext ownerContext = QuotaOwnerContext.builder().role(Role.RECRUITER).companyId(1).build();
            when(quotaService.resolveOwnerContext()).thenReturn(ownerContext);
            when(subscriptionService.findEligibleSubscriptions(any(), any()))
                    .thenReturn(List.of());

            // When
            List<FeatureUsageResponse> result = usageService.getCurrentUsage();

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should include renew date for PER_MONTH quota")
        void shouldIncludeRenewDateForPerMonthQuota() {
            // Given
            QuotaOwnerContext ownerContext = QuotaOwnerContext.builder().role(Role.RECRUITER).companyId(1).build();
            LocalDateTime startDate = LocalDateTime.of(2026, 1, 15, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(2026, 12, 31, 23, 59);
            LocalDateTime now = LocalDateTime.of(2026, 3, 11, 10, 0);

            Subscription subscription = createSubscription(1, startDate, endDate);
            Feature feature = createFeature(1, "AI_MATCHING", "AI Matching", UsageType.EVENT);
            UsageLimit limit = createUsageLimit(feature, 100, UsageLimitUnit.PER_MONTH);

            when(quotaService.resolveOwnerContext()).thenReturn(ownerContext);
            when(subscriptionService.findEligibleSubscriptions(any(), any()))
                    .thenReturn(List.of(subscription));
            when(usageLimitRepository.findAllByPlanIdInWithFeature(any()))
                    .thenReturn(List.of(limit));
            when(usageEventRepository.sumInPeriod(any(), any(), any(), any(), any()))
                    .thenReturn(0L);

            // When
            List<FeatureUsageResponse> result = usageService.getCurrentUsage();

            // Then
            assertThat(result).hasSize(1);
            FeatureUsageResponse response = result.get(0);

            // Renew date cho PER_MONTH (startDate=15/01, now=11/03) là 15/04
            assertThat(response.getRenewDate())
                    .as("Renew date for PER_MONTH should be calculated from subscription startDate")
                    .isEqualTo(LocalDateTime.of(2026, 4, 15, 0, 0));
        }

        @Test
        @DisplayName("Should include subscription endDate as renew date for TOTAL quota")
        void shouldIncludeSubscriptionEndDateAsRenewDateForTotalQuota() {
            // Given
            QuotaOwnerContext ownerContext = QuotaOwnerContext.builder().role(Role.RECRUITER).companyId(1).build();
            LocalDateTime startDate = LocalDateTime.of(2026, 1, 1, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(2026, 12, 31, 23, 59);

            Subscription subscription = createSubscription(1, startDate, endDate);
            Feature feature = createFeature(1, "CV_UPLOAD", "CV Upload", UsageType.EVENT);
            UsageLimit limit = createUsageLimit(feature, 50, UsageLimitUnit.TOTAL);

            when(quotaService.resolveOwnerContext()).thenReturn(ownerContext);
            when(subscriptionService.findEligibleSubscriptions(any(), any()))
                    .thenReturn(List.of(subscription));
            when(usageLimitRepository.findAllByPlanIdInWithFeature(any()))
                    .thenReturn(List.of(limit));
            when(usageEventRepository.sumTotal(any(), any(), any()))
                    .thenReturn(0L);

            // When
            List<FeatureUsageResponse> result = usageService.getCurrentUsage();

            // Then
            assertThat(result).hasSize(1);
            FeatureUsageResponse response = result.get(0);

            assertThat(response.getRenewDate())
                    .as("Renew date for TOTAL should be subscription endDate")
                    .isEqualTo(endDate);
        }

        @Test
        @DisplayName("Should return earliest renew date when multiple subscriptions with different end dates")
        void shouldReturnEarliestRenewDateWithMultipleSubscriptions() {
            // Given
            QuotaOwnerContext ownerContext = QuotaOwnerContext.builder().role(Role.RECRUITER).companyId(1).build();
            LocalDateTime startDate = LocalDateTime.of(2026, 1, 1, 0, 0);

            // 3 subscriptions với endDate khác nhau
            Subscription sub1 = createSubscription(1, startDate, LocalDateTime.of(2026, 6, 30, 23, 59));
            Subscription sub2 = createSubscription(2, startDate, LocalDateTime.of(2026, 3, 31, 23, 59));
            Subscription sub3 = createSubscription(3, startDate, LocalDateTime.of(2026, 12, 31, 23, 59));

            Feature feature = createFeature(1, "TOTAL_FEATURE", "Total Feature", UsageType.EVENT);
            UsageLimit limit = createUsageLimit(feature, 100, UsageLimitUnit.TOTAL);

            when(quotaService.resolveOwnerContext()).thenReturn(ownerContext);
            when(subscriptionService.findEligibleSubscriptions(any(), any()))
                    .thenReturn(List.of(sub1, sub2, sub3));
            when(usageLimitRepository.findAllByPlanIdInWithFeature(any()))
                    .thenReturn(List.of(limit));
            when(usageEventRepository.sumTotal(any(), any(), any()))
                    .thenReturn(0L);

            // When
            List<FeatureUsageResponse> result = usageService.getCurrentUsage();

            // Then: Renew date là endDate sớm nhất (31/03)
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRenewDate())
                    .as("Should return earliest subscription endDate")
                    .isEqualTo(LocalDateTime.of(2026, 3, 31, 23, 59));
        }

        @Test
        @DisplayName("Should advance renew date when simulating time progression")
        void shouldAdvanceRenewDateWhenSimulatingTimeProgression() {
            // Given
            QuotaOwnerContext ownerContext = QuotaOwnerContext.builder().role(Role.RECRUITER).companyId(1).build();
            LocalDateTime startDate = LocalDateTime.of(2026, 1, 20, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(2026, 12, 31, 23, 59);

            Subscription subscription = createSubscription(1, startDate, endDate);
            Feature feature = createFeature(1, "PER_MONTH_FEATURE", "Per Month Feature", UsageType.EVENT);
            UsageLimit limit = createUsageLimit(feature, 100, UsageLimitUnit.PER_MONTH);

            when(quotaService.resolveOwnerContext()).thenReturn(ownerContext);
            when(subscriptionService.findEligibleSubscriptions(any(), any()))
                    .thenReturn(List.of(subscription));
            when(usageLimitRepository.findAllByPlanIdInWithFeature(any()))
                    .thenReturn(List.of(limit));
            when(usageEventRepository.sumInPeriod(any(), any(), any(), any(), any()))
                    .thenReturn(0L);

            // When: Lấy usage ở thời điểm hiện tại (tháng 3, ngày 15)
            // startDate=20/01, now=15/03
            // monthsBetween=2, periodAnchor=20/03 > today(15/03)? YES (lùi 1 tháng)
            // -> periodStart=20/02, periodEnd=20/03
            List<FeatureUsageResponse> marchResult = usageService.getCurrentUsage();

            // Then: Renew date là 20/03 (đang ở trong period Feb 20 - Mar 20)
            assertThat(marchResult.get(0).getRenewDate())
                    .as("March renew date (on March 15, current period ends March 20)")
                    .isEqualTo(LocalDateTime.of(2026, 3, 20, 0, 0));
        }

        @Test
        @DisplayName("Should handle mixed quota types with different renew logic")
        void shouldHandleMixedQuotaTypesWithDifferentRenewLogic() {
            // Given
            QuotaOwnerContext ownerContext = QuotaOwnerContext.builder().role(Role.RECRUITER).companyId(1).build();
            LocalDateTime startDate = LocalDateTime.of(2026, 1, 15, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(2026, 12, 31, 23, 59);

            Subscription subscription = createSubscription(1, startDate, endDate);

            Feature perMonthFeature = createFeature(1, "PER_MONTH", "Per Month", UsageType.EVENT);
            UsageLimit perMonthLimit = createUsageLimit(perMonthFeature, 100, UsageLimitUnit.PER_MONTH);

            Feature totalFeature = createFeature(2, "TOTAL", "Total", UsageType.EVENT);
            UsageLimit totalLimit = createUsageLimit(totalFeature, 50, UsageLimitUnit.TOTAL);

            when(quotaService.resolveOwnerContext()).thenReturn(ownerContext);
            when(subscriptionService.findEligibleSubscriptions(any(), any()))
                    .thenReturn(List.of(subscription));
            when(usageLimitRepository.findAllByPlanIdInWithFeature(any()))
                    .thenReturn(List.of(perMonthLimit, totalLimit));
            when(usageEventRepository.sumInPeriod(any(), any(), any(), any(), any()))
                    .thenReturn(0L);
            when(usageEventRepository.sumTotal(any(), any(), any()))
                    .thenReturn(0L);

            // When
            List<FeatureUsageResponse> result = usageService.getCurrentUsage();

            // Then
            assertThat(result).hasSize(2);

            // Tìm response theo featureKey
            FeatureUsageResponse perMonthResponse = result.stream()
                    .filter(r -> r.getFeatureKey().equals("PER_MONTH"))
                    .findFirst()
                    .orElseThrow();

            FeatureUsageResponse totalResponse = result.stream()
                    .filter(r -> r.getFeatureKey().equals("TOTAL"))
                    .findFirst()
                    .orElseThrow();

            // PER_MONTH: renew date là 15/04
            assertThat(perMonthResponse.getRenewDate())
                    .as("PER_MONTH renew date should be based on monthly cycle")
                    .isEqualTo(LocalDateTime.of(2026, 4, 15, 0, 0));

            // TOTAL: renew date là subscription endDate
            assertThat(totalResponse.getRenewDate())
                    .as("TOTAL renew date should be subscription endDate")
                    .isEqualTo(endDate);
        }
    }
}
