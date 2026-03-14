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
    Page<Criteria> findByNameContainingIgnoreCaseAndCompanyIdAndActive(String name, Integer companyId, Boolean active, Pageable pageable);
    Page<Criteria> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Set<Criteria> findByIsDefault(Boolean isDefault);
    Page<Criteria> findByCompanyIdAndActive(Integer companyId, Boolean active, Pageable pageable);
}
