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
            select s
            from Subscription s
            join fetch s.plan p
            where s.candidate.id = :candidateId
                and s.status = :status
                and :now between s.startDate and s.endDate
            order by
                s.purchasedAt asc,
                s.id asc
            """)
    List<Subscription> findEligibleByCandidateId(@Param("candidateId") Integer candidateId,
                                                 @Param("status") SubscriptionStatus status,
                                                 @Param("now") LocalDateTime now);

    @Query("""
            select s
            from Subscription s
            join fetch s.plan p
            where s.company.id = :companyId
                and s.status = :status
                and :now between s.startDate and s.endDate
            order by
                s.purchasedAt asc,
                s.id asc
            """)
    List<Subscription> findEligibleByCompanyId(@Param("companyId") Integer companyId,
                                               @Param("status") SubscriptionStatus status,
                                               @Param("now") LocalDateTime now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Subscription s where s.id = :subscriptionId")
    Optional<Subscription> lockById(@Param("subscriptionId") Integer subscriptionId);
}
