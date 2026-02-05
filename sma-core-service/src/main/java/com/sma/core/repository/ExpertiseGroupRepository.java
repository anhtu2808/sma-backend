package com.sma.core.repository;

import com.sma.core.entity.JobExpertiseGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertiseGroupRepository extends JpaRepository<JobExpertiseGroup, Integer> {
    boolean existsByName(String name);
}
