package com.sma.core.repository;

import com.sma.core.enums.EventSource;
import com.sma.core.entity.UsageEvent;
import com.sma.core.enums.FeatureKey;
import com.sma.core.enums.UsageEventStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsageEventRepository extends JpaRepository<UsageEvent, Integer>, JpaSpecificationExecutor<UsageEvent> {

    @Query("""
            SELECT COALESCE(SUM(ue.amount), 0)
            FROM UsageEvent ue
            WHERE ue.subscription.id = :subscriptionId
                AND ue.feature.id = :featureId
                AND ue.status = :status
            """)
    Long sumTotal(@Param("subscriptionId") Integer subscriptionId,
                  @Param("featureId") Integer featureId,
                  @Param("status") UsageEventStatus status);

    @Query("""
            SELECT COALESCE(SUM(ue.amount), 0)
            FROM UsageEvent ue
            WHERE ue.subscription.id = :subscriptionId
                AND ue.feature.id = :featureId
                AND ue.createdAt >= :periodStart
                AND ue.createdAt < :periodEnd
                AND ue.status = :status
            """)
    Long sumInPeriod(@Param("subscriptionId") Integer subscriptionId,
                     @Param("featureId") Integer featureId,
                     @Param("periodStart") LocalDateTime periodStart,
                     @Param("periodEnd") LocalDateTime periodEnd,
                     @Param("status") UsageEventStatus status);

    @Query("""
            SELECT COALESCE(SUM(ue.amount), 0)
            FROM UsageEvent ue
            JOIN ue.contexts ctx
            WHERE ue.subscription.id = :subscriptionId
                AND ue.feature.id = :featureId
                AND ue.status = :status
                AND ctx.eventSource = :eventSource
                AND ctx.sourceId = :sourceId
            """)
    Long sumTotalByContext(@Param("subscriptionId") Integer subscriptionId,
                           @Param("featureId") Integer featureId,
                           @Param("eventSource") EventSource eventSource,
                           @Param("sourceId") Integer sourceId,
                           @Param("status") UsageEventStatus status);

    @Query("""
            SELECT COALESCE(SUM(ue.amount), 0)
            FROM UsageEvent ue
            JOIN ue.contexts ctx
            WHERE ue.subscription.id = :subscriptionId
                AND ue.feature.id = :featureId
                AND ue.createdAt >= :periodStart
                AND ue.createdAt < :periodEnd
                AND ue.status = :status
                AND ctx.eventSource = :eventSource
                AND ctx.sourceId = :sourceId
            """)
    Long sumInPeriodByContext(@Param("subscriptionId") Integer subscriptionId,
                              @Param("featureId") Integer featureId,
                              @Param("periodStart") LocalDateTime periodStart,
                              @Param("periodEnd") LocalDateTime periodEnd,
                              @Param("eventSource") EventSource eventSource,
                              @Param("sourceId") Integer sourceId,
                              @Param("status") UsageEventStatus status);

    @Query("""
            SELECT ue.id
            FROM UsageEvent ue
            JOIN ue.contexts ctx
            WHERE ue.feature.featureKey = :featureKey
                AND ctx.eventSource = :eventSource
                AND ctx.sourceId = :sourceId
                AND ue.createdAt >= :createdAtFrom
            ORDER BY ue.createdAt DESC, ue.id DESC
            """)
    List<Integer> findUsageEventIdsByFeatureKeyAndContextAfter(
            @Param("featureKey") String featureKey,
            @Param("eventSource") EventSource eventSource,
            @Param("sourceId") Integer sourceId,
            @Param("createdAtFrom") LocalDateTime createdAtFrom,
            Pageable pageable
    );

    default Optional<Integer> findLatestResumeParsingUsageEventId(Integer resumeId, LocalDateTime parseRequestedAt) {
        if (resumeId == null || parseRequestedAt == null) {
            return Optional.empty();
        }

        return findUsageEventIdsByFeatureKeyAndContextAfter(
                FeatureKey.RESUME_PARSING.name(),
                EventSource.RESUME,
                resumeId,
                parseRequestedAt,
                PageRequest.of(0, 1)
        ).stream().findFirst();
    }

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE UsageEvent ue
            SET ue.status = :status
            WHERE ue.id = :usageEventId
                AND ue.status <> :status
            """)
    int markStatus(@Param("usageEventId") Integer usageEventId,
                   @Param("status") UsageEventStatus status);
}
