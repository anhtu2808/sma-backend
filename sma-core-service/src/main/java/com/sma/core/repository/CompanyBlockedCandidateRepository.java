package com.sma.core.repository;

import com.sma.core.entity.CompanyBlockedCandidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CompanyBlockedCandidateRepository extends JpaRepository<CompanyBlockedCandidate, Integer> {

    boolean existsByCompanyIdAndCandidateIdAndIsDeletedFalse(Integer companyId, Integer candidateId);

    Optional<CompanyBlockedCandidate> findByCompanyIdAndCandidateIdAndIsDeletedFalse(Integer companyId, Integer candidateId);

    @Query("SELECT c FROM CompanyBlockedCandidate c " +
            "JOIN FETCH c.candidate cand " +
            "JOIN FETCH cand.user candUser " +
            "JOIN FETCH c.createdBy creator " +
            "WHERE c.company.id = :companyId " +
            "AND c.isDeleted = false " +
            "AND (:keyword IS NULL OR " +
            "LOWER(candUser.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(candUser.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.reason) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(creator.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<CompanyBlockedCandidate> findAllActiveByCompany(
            @Param("companyId") Integer companyId,
            @Param("keyword") String keyword,
            Pageable pageable);
}
