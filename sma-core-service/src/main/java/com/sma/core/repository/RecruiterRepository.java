package com.sma.core.repository;

import com.sma.core.entity.Recruiter;
import com.sma.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecruiterRepository extends JpaRepository<Recruiter, Integer> {
    Optional<Recruiter> findByCompanyId(Integer companyId);
    long countByCompanyId(Integer companyId);
    Optional<Recruiter> findByUserId(Integer userId);
    @Query("SELECT r.user FROM Recruiter r WHERE r.company.id = :companyId AND r.isRootRecruiter = true")
    Optional<User> findRootUserByCompanyId(@Param("companyId") Integer companyId);
}
