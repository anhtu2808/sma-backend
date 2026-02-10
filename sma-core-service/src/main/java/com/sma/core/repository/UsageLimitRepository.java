package com.sma.core.repository;

import com.sma.core.entity.UsageLimit;
import com.sma.core.entity.UsageLimit.UsageLimitId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsageLimitRepository extends JpaRepository<UsageLimit, UsageLimitId> {
    List<UsageLimit> findAllByPlanId(Integer planId);
    boolean existsByPlanId(Integer planId);
    boolean existsByPlanIdAndFeatureId(Integer planId, Integer featureId);
    Optional<UsageLimit> findByPlanIdAndFeatureId(Integer planId, Integer featureId);
}
