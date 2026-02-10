package com.sma.core.repository;

import com.sma.core.entity.PlanPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanPriceRepository extends JpaRepository<PlanPrice, Integer> {
    List<PlanPrice> findAllByPlanId(Integer planId);

    boolean existsByPlanId(Integer planId);
}
