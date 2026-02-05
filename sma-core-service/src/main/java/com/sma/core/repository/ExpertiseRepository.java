package com.sma.core.repository;

import com.sma.core.entity.JobExpertise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertiseRepository extends JpaRepository<JobExpertise, Integer> {
    boolean existsByNameAndExpertiseGroupId(String name, Integer groupId);
}
