package com.sma.core.repository;

import com.sma.core.entity.Plan;
import com.sma.core.enums.PlanTarget;
import com.sma.core.enums.PlanType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Integer>, JpaSpecificationExecutor<Plan> {
    boolean existsByNameIgnoreCase(String name);

    Optional<Plan> findFirstByPlanTargetAndPlanTypeAndIsDefaultTrueAndIsActiveTrue(PlanTarget planTarget, PlanType planType);

    @Modifying
    @Query("""
            UPDATE Plan p
            SET p.isDefault = false
            WHERE p.planTarget = :planTarget
              AND p.id <> :excludeId
            """)
    int clearDefaultByTarget(@Param("planTarget") PlanTarget planTarget, @Param("excludeId") Integer excludeId);
}
