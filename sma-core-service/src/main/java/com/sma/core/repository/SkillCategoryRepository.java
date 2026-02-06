package com.sma.core.repository;

import com.sma.core.entity.SkillCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillCategoryRepository extends JpaRepository<SkillCategory, Integer> {
    boolean existsByName(String name);
    Page<SkillCategory> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
