package com.sma.core.repository;

import com.sma.core.entity.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillCategoryRepository extends JpaRepository<SkillCategory, Integer> {
    boolean existsByName(String name);
}
