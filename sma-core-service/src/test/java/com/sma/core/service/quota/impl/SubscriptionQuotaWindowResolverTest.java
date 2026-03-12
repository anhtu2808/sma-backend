package com.sma.core.service.quota.impl;

import com.sma.core.entity.Company;
import com.sma.core.entity.Plan;
import com.sma.core.entity.Subscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SubscriptionQuotaWindowResolver Tests")
class SubscriptionQuotaWindowResolverTest {

    private SubscriptionQuotaWindowResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new SubscriptionQuotaWindowResolver();
    }

    @Nested
    @DisplayName("resolveAnchoredMonthlyWindow - Boundary Cases")
    class ResolveAnchoredMonthlyWindowBoundaryTests {

        private Subscription createSubscription(LocalDateTime startDate) {
            return Subscription.builder()
                    .id(1)
                    .startDate(startDate)
                    .endDate(startDate.plusYears(1))
                    .plan(Plan.builder().id(1).build())
                    .company(Company.builder().id(1).build())
                    .build();
        }

        @ParameterizedTest(name = "[{index}] anchor={0}, now={1} => periodStart={2}, periodEnd={3}")
        @CsvSource({
                // Ngày đầu tháng: 
                // anchor=01/01, now=15/03 -> monthsBetween=2 -> periodAnchor=01/03 <= today(15/03) 
                // -> periodStart=01/03, periodEnd=01/04
                "2026-01-01T00:00, 2026-03-15T10:00, 2026-03-01T00:00, 2026-04-01T00:00",
                // Ngày giữa tháng: 
                // anchor=15/01, now=20/03 -> monthsBetween=2 -> periodAnchor=15/03 <= today(20/03)
                // -> periodStart=15/03, periodEnd=15/04
                "2026-01-15T00:00, 2026-03-20T10:00, 2026-03-15T00:00, 2026-04-15T00:00",
                // Ngày 31/01 với now=28/02: 
                // monthsBetween=1 -> periodAnchor=28/02 (Jan31+1month=Feb28) <= today(28/02)
                // -> periodStart=28/02, periodEnd=28/03
                "2026-01-31T00:00, 2026-02-28T10:00, 2026-02-28T00:00, 2026-03-28T00:00",
                // Năm nhuận 2024: anchor=29/01, now=29/02
                // monthsBetween=1 -> periodAnchor=29/02 (leap year!) <= today(29/02)
                // -> periodStart=29/02, periodEnd=29/03
                "2024-01-29T00:00, 2024-02-29T10:00, 2024-02-29T00:00, 2024-03-29T00:00",
                // Chuyển năm: anchor=15/06/2026, now=20/12/2026
                // monthsBetween=6 -> periodAnchor=15/12 <= today(20/12)
                // -> periodStart=15/12, periodEnd=15/01/2027
                "2026-06-15T00:00, 2026-12-20T10:00, 2026-12-15T00:00, 2027-01-15T00:00",
                // Cùng ngày anchor sau 1 năm
                // anchor=15/01/2026, now=15/01/2027
                // monthsBetween=12 -> periodAnchor=15/01/2027 <= today(15/01/2027)
                // -> periodStart=15/01/2027, periodEnd=15/02/2027
                "2026-01-15T00:00, 2027-01-15T10:00, 2027-01-15T00:00, 2027-02-15T00:00",
                // Giữa tháng 12 chuyển sang năm mới
                // anchor=20/12/2026, now=25/12/2026
                // monthsBetween=0 -> periodAnchor=20/12 <= today(25/12)
                // -> periodStart=20/12, periodEnd=20/01/2027
                "2026-12-20T00:00, 2026-12-25T10:00, 2026-12-20T00:00, 2027-01-20T00:00",
                // Ngày 31/03 -> 30/04 (tháng 4 chỉ có 30 ngày)
                // anchor=31/03, now=15/04
                // monthsBetween=1 -> periodAnchor=30/04 (Mar31+1month=Apr30) > today(15/04)!
                // -> periodAnchor=31/03 (lùi 1 tháng)
                // -> periodStart=31/03, periodEnd=30/04
                "2026-03-31T00:00, 2026-04-15T10:00, 2026-03-31T00:00, 2026-04-30T00:00",
                // Ngày 31/05 -> 30/06
                // anchor=31/05, now=15/06
                // monthsBetween=1 -> periodAnchor=30/06 (May31+1month=Jun30) > today(15/06)!
                // -> periodAnchor=31/05 (lùi 1 tháng)
                // -> periodStart=31/05, periodEnd=30/06
                "2026-05-31T00:00, 2026-06-15T10:00, 2026-05-31T00:00, 2026-06-30T00:00",
                // Ngày 30/04 -> 30/05
                // anchor=30/04, now=15/05
                // monthsBetween=1 -> periodAnchor=30/05 <= today(15/05)? NO! 30/05 > 15/05
                // -> periodAnchor=30/04 (lùi 1 tháng)
                // -> periodStart=30/04, periodEnd=30/05
                "2026-04-30T00:00, 2026-05-15T10:00, 2026-04-30T00:00, 2026-05-30T00:00",
        })
        @DisplayName("Should calculate correct window for various date combinations")
        void shouldCalculateCorrectWindow(
                String anchorStr,
                String nowStr,
                String expectedStartStr,
                String expectedEndStr) {
            // Given
            LocalDateTime anchorDate = LocalDateTime.parse(anchorStr);
            LocalDateTime now = LocalDateTime.parse(nowStr);
            LocalDateTime expectedStart = LocalDateTime.parse(expectedStartStr);
            LocalDateTime expectedEnd = LocalDateTime.parse(expectedEndStr);

            Subscription subscription = createSubscription(anchorDate);

            // When
            var window = resolver.resolveAnchoredMonthlyWindow(subscription, now);

            // Then
            assertThat(window.periodStart())
                    .as("Period start should be %s", expectedStart)
                    .isEqualTo(expectedStart);
            assertThat(window.periodEnd())
                    .as("Period end should be %s (this is the renew date)", expectedEnd)
                    .isEqualTo(expectedEnd);
        }

        @Test
        @DisplayName("Should advance renew date when crossing to next month")
        void shouldAdvanceRenewDateWhenCrossingMonth() {
            // Given: Subscription bắt đầu ngày 15/01/2026
            LocalDateTime startDate = LocalDateTime.of(2026, 1, 15, 0, 0);
            Subscription subscription = createSubscription(startDate);

            // When: Hiện tại là 11/03/2026
            // monthsBetween = 2, periodAnchor = 15/03 > today(11/03)? YES!
            // -> periodStart = 15/02, periodEnd = 15/03
            LocalDateTime marchNow = LocalDateTime.of(2026, 3, 11, 10, 0);
            var marchWindow = resolver.resolveAnchoredMonthlyWindow(subscription, marchNow);

            // When: Hiện tại là 20/04/2026 (đã qua renew date 15/04)
            // monthsBetween = 3, periodAnchor = 15/04 <= today(20/04)? YES!
            // -> periodStart = 15/04, periodEnd = 15/05
            LocalDateTime aprilNow = LocalDateTime.of(2026, 4, 20, 10, 0);
            var aprilWindow = resolver.resolveAnchoredMonthlyWindow(subscription, aprilNow);

            // Then
            assertThat(marchWindow.periodEnd())
                    .as("March (11/03) renew date should be March 15 (current period ends)")
                    .isEqualTo(LocalDateTime.of(2026, 3, 15, 0, 0));

            assertThat(aprilWindow.periodEnd())
                    .as("April (20/04) renew date should be May 15")
                    .isEqualTo(LocalDateTime.of(2026, 5, 15, 0, 0));
        }

        @Test
        @DisplayName("Should handle leap year February correctly")
        void shouldHandleLeapYearFebruary() {
            // Given: Năm nhuận 2024, ngày 29/01
            LocalDateTime startDate = LocalDateTime.of(2024, 1, 29, 0, 0);
            Subscription subscription = createSubscription(startDate);

            // When: Ngày 29/02/2024 (năm nhuận)
            // monthsBetween = 1, periodAnchor = 29/02 (leap year) <= today(29/02)? YES!
            // -> periodStart = 29/02, periodEnd = 29/03
            LocalDateTime leapDay = LocalDateTime.of(2024, 2, 29, 10, 0);
            var window = resolver.resolveAnchoredMonthlyWindow(subscription, leapDay);

            // Then
            assertThat(window.periodStart())
                    .as("Period start should be Feb 29 in leap year")
                    .isEqualTo(LocalDateTime.of(2024, 2, 29, 0, 0));
            assertThat(window.periodEnd())
                    .as("Period end should be Mar 29")
                    .isEqualTo(LocalDateTime.of(2024, 3, 29, 0, 0));
        }

        @Test
        @DisplayName("Should handle non-leap year February boundary")
        void shouldHandleNonLeapYearFebruaryBoundary() {
            // Given: Năm không nhuận 2026, ngày 31/01
            LocalDateTime startDate = LocalDateTime.of(2026, 1, 31, 0, 0);
            Subscription subscription = createSubscription(startDate);

            // When: Ngày 28/02/2026 (tháng 2 năm thường)
            // monthsBetween = 1, periodAnchor = 28/02 (Jan31+1month=Feb28) <= today(28/02)? YES!
            // -> periodStart = 28/02, periodEnd = 28/03
            LocalDateTime feb28 = LocalDateTime.of(2026, 2, 28, 10, 0);
            var window = resolver.resolveAnchoredMonthlyWindow(subscription, feb28);

            // Then
            assertThat(window.periodStart())
                    .as("Period start should be Feb 28 in non-leap year")
                    .isEqualTo(LocalDateTime.of(2026, 2, 28, 0, 0));
            assertThat(window.periodEnd())
                    .as("Period end should be Mar 28")
                    .isEqualTo(LocalDateTime.of(2026, 3, 28, 0, 0));
        }

        @ParameterizedTest(name = "[{index}] anchor={0}, now={1} => periodStart={2}, periodEnd={3}")
        @CsvSource({
                // Tháng có 31 ngày (May) -> tháng có 30 ngày (Jun)
                // anchor=31/05, now=15/06 -> periodAnchor=30/06 > 15/06 -> periodStart=31/05, periodEnd=30/06
                "2026-05-31T00:00, 2026-06-15T10:00, 2026-05-31T00:00, 2026-06-30T00:00",
                // Tháng có 30 ngày (Apr) -> tháng có 31 ngày (May)
                // anchor=30/04, now=15/05 -> periodAnchor=30/05 > 15/05 -> periodStart=30/04, periodEnd=30/05
                "2026-04-30T00:00, 2026-05-15T10:00, 2026-04-30T00:00, 2026-05-30T00:00",
                // Tháng 2 năm nhuận (29 ngày) -> tháng 3 (31 ngày)
                // anchor=29/02/2024, now=15/03/2024 -> periodAnchor=29/03 > 15/03 -> periodStart=29/02, periodEnd=29/03
                "2024-02-29T00:00, 2024-03-15T10:00, 2024-02-29T00:00, 2024-03-29T00:00",
        })
        @DisplayName("Should handle months with different day counts")
        void shouldHandleMonthsWithDifferentDayCounts(
                String anchorStr,
                String nowStr,
                String expectedStartStr,
                String expectedEndStr) {
            // Given
            LocalDateTime anchorDate = LocalDateTime.parse(anchorStr);
            LocalDateTime now = LocalDateTime.parse(nowStr);
            LocalDateTime expectedStart = LocalDateTime.parse(expectedStartStr);
            LocalDateTime expectedEnd = LocalDateTime.parse(expectedEndStr);

            Subscription subscription = createSubscription(anchorDate);

            // When
            var window = resolver.resolveAnchoredMonthlyWindow(subscription, now);

            // Then
            assertThat(window.periodStart()).isEqualTo(expectedStart);
            assertThat(window.periodEnd())
                    .as("Renew date should adjust for month length differences")
                    .isEqualTo(expectedEnd);
        }
    }
}
