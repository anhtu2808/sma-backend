package com.sma.core.repository;

import com.sma.core.entity.Criteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CriteriaRepository extends JpaRepository<Criteria, Integer> {
    Page<Criteria> findByNameContainingIgnoreCaseAnAndCompanyIdAndActive(String name, Integer companyId, boolean isActive, Pageable pageable);
    Page<Criteria> findByNameContainingIgnoreCaseAn(String name, Pageable pageable);
    Set<Criteria> findByDefault(boolean isDefault);
    Page<Criteria> findByCompanyIdAndActive(Integer companyId, boolean isActive, Pageable pageable);
}
