package com.sma.core.repository;

import com.sma.core.entity.UsageLimit;
import com.sma.core.entity.UsageLimit.UsageLimitId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsageLimitRepository extends JpaRepository<UsageLimit, UsageLimitId> {
    List<UsageLimit> findAllByPlanId(Integer planId);
    boolean existsByPlanId(Integer planId);
}
