package com.sma.core.repository;

import com.sma.core.entity.JobExpertise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertiseRepository extends JpaRepository<JobExpertise, Integer> {
    boolean existsByNameAndExpertiseGroupId(String name, Integer groupId);
    Page<JobExpertise> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<JobExpertise> findByExpertiseGroup_NameContainingIgnoreCase(String groupName, Pageable pageable);
    Page<JobExpertise> findByNameContainingIgnoreCaseOrExpertiseGroup_NameContainingIgnoreCase(
            String name, String groupName, Pageable pageable);
    Page<JobExpertise> findByNameContainingIgnoreCaseAndExpertiseGroup_Id(String name, Integer groupId, Pageable pageable);
    Page<JobExpertise> findByExpertiseGroup_Id(Integer groupId, Pageable pageable);
}
