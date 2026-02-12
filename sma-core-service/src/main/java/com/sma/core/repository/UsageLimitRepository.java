package com.sma.core.repository;

import com.sma.core.entity.UsageLimit;
import com.sma.core.entity.UsageLimit.UsageLimitId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsageLimitRepository extends JpaRepository<UsageLimit, UsageLimitId> {
    List<UsageLimit> findAllByPlanId(Integer planId);
    boolean existsByPlanId(Integer planId);
    boolean existsByPlanIdAndFeatureId(Integer planId, Integer featureId);
    Optional<UsageLimit> findByPlanIdAndFeatureId(Integer planId, Integer featureId);
    boolean existsByPlanIdInAndFeatureId(Collection<Integer> planIds, Integer featureId);
    List<UsageLimit> findAllByPlanIdInAndFeatureId(Collection<Integer> planIds, Integer featureId);

    @Query("""
            select coalesce(sum(ul.maxQuota), 0)
            from UsageLimit ul
            where ul.plan.id in :planIds and ul.feature.id = :featureId
            """)
    Long sumMaxQuotaByPlanIdInAndFeatureId(@Param("planIds") Collection<Integer> planIds,
                                           @Param("featureId") Integer featureId);
}
