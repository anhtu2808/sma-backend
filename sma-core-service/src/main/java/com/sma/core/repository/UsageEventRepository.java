package com.sma.core.repository;

import com.sma.core.enums.EventSource;
import com.sma.core.entity.UsageEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UsageEventRepository extends JpaRepository<UsageEvent, Integer>, JpaSpecificationExecutor<UsageEvent> {

    @Query("""
            SELECT COALESCE(SUM(ue.amount), 0)
            FROM UsageEvent ue
            WHERE ue.subscription.id = :subscriptionId
                AND ue.feature.id = :featureId
            """)
    Long sumTotal(@Param("subscriptionId") Integer subscriptionId, @Param("featureId") Integer featureId);

    @Query("""
            SELECT COALESCE(SUM(ue.amount), 0)
            FROM UsageEvent ue
            WHERE ue.subscription.id = :subscriptionId
                AND ue.feature.id = :featureId
                AND ue.createdAt >= :periodStart
                AND ue.createdAt < :periodEnd
            """)
    Long sumInPeriod(@Param("subscriptionId") Integer subscriptionId,
                     @Param("featureId") Integer featureId,
                     @Param("periodStart") LocalDateTime periodStart,
                     @Param("periodEnd") LocalDateTime periodEnd);

    @Query("""
            SELECT COALESCE(SUM(ue.amount), 0)
            FROM UsageEvent ue
            WHERE ue.subscription.id = :subscriptionId
                AND ue.feature.id = :featureId
                AND ue.eventSource = :eventSource
                AND ue.sourceId = :entityId
            """)
    Long sumTotalByContext(@Param("subscriptionId") Integer subscriptionId,
                           @Param("featureId") Integer featureId,
                           @Param("eventSource") EventSource eventSource,
                           @Param("sourceId") Integer entityId);

    @Query("""
            SELECT COALESCE(SUM(ue.amount), 0)
            FROM UsageEvent ue
            WHERE ue.subscription.id = :subscriptionId
                AND ue.feature.id = :featureId
                AND ue.createdAt >= :periodStart
                AND ue.createdAt < :periodEnd
                AND ue.eventSource = :eventSource
                AND ue.sourceId = :entityId
            """)
    Long sumInPeriodByContext(@Param("subscriptionId") Integer subscriptionId,
                              @Param("featureId") Integer featureId,
                              @Param("periodStart") LocalDateTime periodStart,
                              @Param("periodEnd") LocalDateTime periodEnd,
                              @Param("eventSource") EventSource eventSource,
                              @Param("sourceId") Integer entityId);
}