package com.sma.core.repository;

import com.sma.core.entity.Subscription;
import com.sma.core.enums.SubscriptionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
    boolean existsByPlanId(Integer planId);

    @Query("""
            SELECT s
            FROM Subscription s
            JOIN FETCH s.plan p
            WHERE s.candidate.id = :candidateId
                AND s.status = :status
                AND :now BETWEEN s.startDate AND s.endDate
            ORDER BY
                s.purchasedAt ASC,
                s.id ASC
            """)
    List<Subscription> findEligibleByCandidateId(@Param("candidateId") Integer candidateId,
                                                 @Param("status") SubscriptionStatus status,
                                                 @Param("now") LocalDateTime now);

    @Query("""
            SELECT s
            FROM Subscription s
            JOIN FETCH s.plan p
            WHERE s.company.id = :companyId
                AND s.status = :status
                AND :now BETWEEN s.startDate AND s.endDate
            ORDER BY
                s.purchasedAt ASC,
                s.id ASC
            """)
    List<Subscription> findEligibleByCompanyId(@Param("companyId") Integer companyId,
                                               @Param("status") SubscriptionStatus status,
                                               @Param("now") LocalDateTime now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Subscription s WHERE s.id = :subscriptionId")
    Optional<Subscription> lockById(@Param("subscriptionId") Integer subscriptionId);
}
