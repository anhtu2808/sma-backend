package com.sma.core.repository;

import com.sma.core.entity.PlanPrice;
import com.sma.core.enums.PlanDurationUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanPriceRepository extends JpaRepository<PlanPrice, Integer> {
    List<PlanPrice> findAllByPlanId(Integer planId);

    boolean existsByPlanId(Integer planId);

    Optional<PlanPrice> findFirstByPlanIdAndUnitAndIsActiveTrue(Integer planId, PlanDurationUnit unit);
}
