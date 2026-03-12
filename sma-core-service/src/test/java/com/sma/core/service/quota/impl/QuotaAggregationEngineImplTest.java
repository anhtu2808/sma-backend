package com.sma.core.service.quota.impl;

import com.sma.core.dto.model.QuotaAggregate;
import com.sma.core.dto.model.QuotaOwnerContext;
import com.sma.core.entity.Company;
import com.sma.core.entity.Feature;
import com.sma.core.entity.Plan;
import com.sma.core.entity.Subscription;
import com.sma.core.entity.UsageLimit;
import com.sma.core.enums.Role;
import com.sma.core.enums.UsageLimitUnit;
import com.sma.core.enums.UsageType;
import com.sma.core.repository.UsageEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@DisplayName("QuotaAggregationEngineImpl Tests")
@ExtendWith(MockitoExtension.class)
class QuotaAggregationEngineImplTest {

    @Mock
    private UsageEventRepository usageEventRepository;

    @Mock
    private ResumeUploadLimitStateChecker resumeUploadLimitStateChecker;

    private QuotaAggregationEngineImpl engine;
    private SubscriptionQuotaWindowResolver windowResolver;
    private EventUsageCalculatorImpl eventUsageCalculator;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        windowResolver = new SubscriptionQuotaWindowResolver();
        eventUsageCalculator = new EventUsageCalculatorImpl(usageEventRepository, windowResolver);
        // Default clock at 2026-03-15T10:00:00Z
        fixedClock = Clock.fixed(Instant.parse("2026-03-15T10:00:00Z"), ZoneId.of("UTC"));
        engine = new QuotaAggregationEngineImpl(
                eventUsageCalculator,
                resumeUploadLimitStateChecker,
                windowResolver,
                fixedClock
        );
    }

    private Subscription createSubscription(LocalDateTime startDate, LocalDateTime endDate) {
        return Subscription.builder()
                .id(1)
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

    private UsageLimit createUsageLimit(Integer maxQuota, UsageLimitUnit unit) {
        return UsageLimit.builder()
                .maxQuota(maxQuota)
                .limitUnit(unit)
                .build();
    }

    private void setClockTo(String instant) {
        fixedClock = Clock.fixed(Instant.parse(instant), ZoneId.of("UTC"));
        engine = new QuotaAggregationEngineImpl(
                eventUsageCalculator,
                resumeUploadLimitStateChecker,
                windowResolver,
                fixedClock
        );
    }

    @Nested
    @DisplayName("Renew Date Calculation - Data Driven Tests")
    class RenewDateCalculationTests {

        static Stream<Arguments> provideRenewDateScenarios() {
            // Using clock at 2026-03-15T10:00:00Z (March 15, 2026)
            return Stream.of(
                    // PER_MONTH: startDate=15/01, now(clock)=15/03 
                    // -> monthsBetween=2 (Jan->Mar)
                    // -> periodAnchor=15/03 <= today(15/03)? YES
                    // -> periodStart=15/03, periodEnd=15/04
                    Arguments.of(
                            UsageLimitUnit.PER_MONTH,
                            LocalDateTime.of(2026, 1, 15, 0, 0),  // startDate
                            "2026-03-15T10:00:00Z",                  // clock instant
                            LocalDateTime.of(2026, 4, 15, 0, 0)    // expected renewDate (periodEnd)
                    ),
                    // PER_MONTH: startDate=01/01, now(clock)=15/03
                    // -> monthsBetween=2
                    // -> periodAnchor=01/03 <= today(15/03)? YES
                    // -> periodStart=01/03, periodEnd=01/04
                    Arguments.of(
                            UsageLimitUnit.PER_MONTH,
                            LocalDateTime.of(2026, 1, 1, 0, 0),
                            "2026-03-15T10:00:00Z",
                            LocalDateTime.of(2026, 4, 1, 0, 0)
                    ),
                    // PER_MONTH: startDate=31/01, now=28/02 (non-leap year)
                    // -> monthsBetween=1
                    // -> periodAnchor=28/02 (Jan31+1month=Feb28) <= today(28/02)? YES
                    // -> periodStart=28/02, periodEnd=28/03
                    Arguments.of(
                            UsageLimitUnit.PER_MONTH,
                            LocalDateTime.of(2026, 1, 31, 0, 0),
                            "2026-02-28T10:00:00Z",
                            LocalDateTime.of(2026, 3, 28, 0, 0)
                    ),
                    // PER_MONTH năm nhuận: startDate=29/01/2024, now=29/02/2024
                    // -> monthsBetween=1
                    // -> periodAnchor=29/02/2024 (leap year!) <= today(29/02)? YES
                    // -> periodStart=29/02, periodEnd=29/03
                    Arguments.of(
                            UsageLimitUnit.PER_MONTH,
                            LocalDateTime.of(2024, 1, 29, 0, 0),
                            "2024-02-29T10:00:00Z",
                            LocalDateTime.of(2024, 3, 29, 0, 0)
                    )
            );
        }

        @ParameterizedTest(name = "[{index}] unit={0}, startDate={1}, clock={2} => renewDate={3}")
        @MethodSource("provideRenewDateScenarios")
        @DisplayName("Should calculate correct renew date for PER_MONTH unit")
        void shouldCalculateCorrectRenewDateForPerMonth(
                UsageLimitUnit unit,
                LocalDateTime startDate,
                String clockInstant,
                LocalDateTime expectedRenewDate) {
            // Given
            setClockTo(clockInstant);
            Feature feature = createFeature(1, "TEST_FEATURE", "Test Feature", UsageType.EVENT);
            Subscription subscription = createSubscription(startDate, startDate.plusYears(1));
            UsageLimit limit = createUsageLimit(100, unit);
            QuotaOwnerContext ownerContext = QuotaOwnerContext.builder()
                    .role(Role.RECRUITER)
                    .companyId(1)
                    .build();

            // Mock repository để trả về 0 usage
            when(usageEventRepository.sumInPeriod(
                    any(), any(), any(LocalDateTime.class), any(LocalDateTime.class), any()))
                    .thenReturn(0L);

            // When
            QuotaAggregate aggregate = engine.aggregate(feature, List.of(subscription), List.of(limit), ownerContext);

            // Then
            assertThat(aggregate.getRenewDate())
                    .as("Renew date should be %s for unit %s", expectedRenewDate, unit)
                    .isEqualTo(expectedRenewDate);
        }

        @Test
        @DisplayName("Should set renew date to subscription endDate for TOTAL unit")
        void shouldSetRenewDateToSubscriptionEndDateForTotalUnit() {
            // Given
            Feature feature = createFeature(1, "TEST_FEATURE", "Test Feature", UsageType.EVENT);
            LocalDateTime startDate = LocalDateTime.of(2026, 1, 15, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(2026, 12, 31, 23, 59);
            Subscription subscription = createSubscription(startDate, endDate);
            UsageLimit limit = createUsageLimit(100, UsageLimitUnit.TOTAL);
            QuotaOwnerContext ownerContext = QuotaOwnerContext.builder()
                    .role(Role.RECRUITER)
                    .companyId(1)
                    .build();

            // Mock repository
            when(usageEventRepository.sumTotal(any(), any(), any()))
                    .thenReturn(0L);

            // When
            QuotaAggregate aggregate = engine.aggregate(feature, List.of(subscription), List.of(limit), ownerContext);

            // Then
            assertThat(aggregate.getRenewDate())
                    .as("Renew date for TOTAL unit should be subscription endDate")
                    .isEqualTo(endDate);
        }

        @Test
        @DisplayName("Should return earliest endDate for TOTAL unit with multiple subscriptions")
        void shouldReturnEarliestEndDateForTotalUnitWithMultipleSubscriptions() {
            // Given
            Feature feature = createFeature(1, "TEST_FEATURE", "Test Feature", UsageType.EVENT);
            LocalDateTime startDate = LocalDateTime.of(2026, 1, 1, 0, 0);

            Subscription sub1 = createSubscription(startDate, LocalDateTime.of(2026, 6, 30, 23, 59));
            sub1.setId(1);
            Subscription sub2 = createSubscription(startDate, LocalDateTime.of(2026, 3, 31, 23, 59));
            sub2.setId(2);
            Subscription sub3 = createSubscription(startDate, LocalDateTime.of(2026, 12, 31, 23, 59));
            sub3.setId(3);

            UsageLimit limit = createUsageLimit(100, UsageLimitUnit.TOTAL);
            QuotaOwnerContext ownerContext = QuotaOwnerContext.builder()
                    .role(Role.RECRUITER)
                    .companyId(1)
                    .build();

            // Mock repository
            when(usageEventRepository.sumTotal(any(), any(), any()))
                    .thenReturn(0L);

            // When
            QuotaAggregate aggregate = engine.aggregate(
                    feature,
                    List.of(sub1, sub2, sub3),
                    List.of(limit),
                    ownerContext
            );

            // Then: Renew date là endDate sớm nhất (31/03)
            assertThat(aggregate.getRenewDate())
                    .as("Renew date should be the earliest subscription endDate")
                    .isEqualTo(LocalDateTime.of(2026, 3, 31, 23, 59));
        }

        @Test
        @DisplayName("Should return null renew date for BOOLEAN usage type")
        void shouldReturnNullRenewDateForBooleanUsageType() {
            // Given
            Feature feature = createFeature(1, "BOOLEAN_FEATURE", "Boolean Feature", UsageType.BOOLEAN);
            Subscription subscription = createSubscription(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 12, 31, 23, 59)
            );
            QuotaOwnerContext ownerContext = QuotaOwnerContext.builder()
                    .role(Role.RECRUITER)
                    .companyId(1)
                    .build();

            // When
            QuotaAggregate aggregate = engine.aggregate(feature, List.of(subscription), List.of(), ownerContext);

            // Then
            assertThat(aggregate.getUsageType()).isEqualTo(UsageType.BOOLEAN);
            assertThat(aggregate.getRenewDate()).isNull();
        }
    }

    @Nested
    @DisplayName("Renew Date Advancement Simulation")
    class RenewDateAdvancementTests {

        @Test
        @DisplayName("Should advance renew date correctly when simulating next month")
        void shouldAdvanceRenewDateWhenSimulatingNextMonth() {
            // Given: Subscription bắt đầu ngày 15/01/2026, PER_MONTH
            LocalDateTime startDate = LocalDateTime.of(2026, 1, 15, 0, 0);
            Subscription subscription = createSubscription(startDate, startDate.plusYears(1));

            // When: Giả lập tháng 3/2026 (15/03)
            // monthsBetween=2, periodAnchor=15/03 <= 15/03? YES
            // -> periodStart=15/03, periodEnd=15/04
            setClockTo("2026-03-15T10:00:00Z");
            var marchWindow = windowResolver.resolveAnchoredMonthlyWindow(subscription, LocalDateTime.now(fixedClock));

            // Then: Renew date tháng 3 là 15/04
            assertThat(marchWindow.periodEnd())
                    .as("March renew date should be April 15")
                    .isEqualTo(LocalDateTime.of(2026, 4, 15, 0, 0));

            // When: Giả lập tháng 4/2026 (20/04)
            // monthsBetween=3, periodAnchor=15/04 <= 20/04? YES
            // -> periodStart=15/04, periodEnd=15/05
            setClockTo("2026-04-20T10:00:00Z");
            var aprilWindow = windowResolver.resolveAnchoredMonthlyWindow(subscription, LocalDateTime.now(fixedClock));

            // Then: Renew date tháng 4 là 15/05
            assertThat(aprilWindow.periodEnd())
                    .as("April renew date should be May 15")
                    .isEqualTo(LocalDateTime.of(2026, 5, 15, 0, 0));
        }

        @Test
        @DisplayName("Should verify renew date progression over multiple months")
        void shouldVerifyRenewDateProgressionOverMultipleMonths() {
            // Given: Subscription bắt đầu ngày 20/01/2026
            LocalDateTime startDate = LocalDateTime.of(2026, 1, 20, 0, 0);
            Subscription subscription = createSubscription(startDate, startDate.plusYears(1));

            // When & Then: Kiểm tra renew date qua các tháng
            // 25/01: monthsBetween=0, periodAnchor=20/01 <= 25/01? YES
            // -> periodStart=20/01, periodEnd=20/02
            setClockTo("2026-01-25T10:00:00Z");
            assertThat(windowResolver.resolveAnchoredMonthlyWindow(
                    subscription, LocalDateTime.now(fixedClock)).periodEnd())
                    .as("January renew date")
                    .isEqualTo(LocalDateTime.of(2026, 2, 20, 0, 0));

            // 15/02: monthsBetween=1, periodAnchor=20/02 > 15/02? YES (lùi 1 tháng)
            // -> periodStart=20/01, periodEnd=20/02
            setClockTo("2026-02-15T10:00:00Z");
            assertThat(windowResolver.resolveAnchoredMonthlyWindow(
                    subscription, LocalDateTime.now(fixedClock)).periodEnd())
                    .as("February (mid-month) renew date - still in January period")
                    .isEqualTo(LocalDateTime.of(2026, 2, 20, 0, 0));

            // 01/03: monthsBetween=2, periodAnchor=20/03 > 01/03? YES (lùi 1 tháng)
            // -> periodStart=20/02, periodEnd=20/03
            setClockTo("2026-03-01T10:00:00Z");
            assertThat(windowResolver.resolveAnchoredMonthlyWindow(
                    subscription, LocalDateTime.now(fixedClock)).periodEnd())
                    .as("March renew date")
                    .isEqualTo(LocalDateTime.of(2026, 3, 20, 0, 0));

            // 25/03: monthsBetween=2, periodAnchor=20/03 <= 25/03? YES
            // -> periodStart=20/03, periodEnd=20/04
            setClockTo("2026-03-25T10:00:00Z");
            assertThat(windowResolver.resolveAnchoredMonthlyWindow(
                    subscription, LocalDateTime.now(fixedClock)).periodEnd())
                    .as("Late March renew date")
                    .isEqualTo(LocalDateTime.of(2026, 4, 20, 0, 0));

            // 15/06: monthsBetween=5, periodAnchor=20/06 > 15/06? YES (lùi 1 tháng)
            // -> periodStart=20/05, periodEnd=20/06
            setClockTo("2026-06-15T10:00:00Z");
            assertThat(windowResolver.resolveAnchoredMonthlyWindow(
                    subscription, LocalDateTime.now(fixedClock)).periodEnd())
                    .as("June renew date")
                    .isEqualTo(LocalDateTime.of(2026, 6, 20, 0, 0));
        }
    }
}
