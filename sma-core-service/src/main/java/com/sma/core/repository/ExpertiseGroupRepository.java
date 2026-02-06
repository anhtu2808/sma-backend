package com.sma.core.repository;

import com.sma.core.entity.JobExpertiseGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertiseGroupRepository extends JpaRepository<JobExpertiseGroup, Integer> {
    boolean existsByName(String name);
    Page<JobExpertiseGroup> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
