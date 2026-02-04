package com.sma.core.repository;

import com.sma.core.entity.Recruiter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecruiterRepository extends JpaRepository<Recruiter, Integer> {
    Optional<Recruiter> findByCompanyId(Integer companyId);
    long countByCompanyId(Integer companyId);
}
