package com.sma.core.repository;

import com.sma.core.entity.UsageEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UsageEventRepository extends JpaRepository<UsageEvent, Integer> {

    @Query("""
            select coalesce(sum(ue.amount), 0)
            from UsageEvent ue
            where ue.subscription.id = :subscriptionId
                and ue.feature.id = :featureId
            """)
    Long sumTotal(@Param("subscriptionId") Integer subscriptionId, @Param("featureId") Integer featureId);

    @Query("""
            select coalesce(sum(ue.amount), 0)
            from UsageEvent ue
            where ue.subscription.id = :subscriptionId
                and ue.feature.id = :featureId
                and ue.createdAt >= :periodStart
                and ue.createdAt < :periodEnd
            """)
    Long sumInPeriod(@Param("subscriptionId") Integer subscriptionId,
                     @Param("featureId") Integer featureId,
                     @Param("periodStart") LocalDateTime periodStart,
                     @Param("periodEnd") LocalDateTime periodEnd);
}
