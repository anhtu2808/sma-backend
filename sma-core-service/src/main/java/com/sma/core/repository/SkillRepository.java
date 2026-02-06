package com.sma.core.repository;

import com.sma.core.entity.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Integer> {
    boolean existsByNameAndCategoryId(String name, Integer categoryId);
    Page<Skill> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Skill> findByNameContainingIgnoreCaseOrCategory_NameContainingIgnoreCase(
            String name,
            String categoryName,
            Pageable pageable
    );
    Page<Skill> findByNameContainingIgnoreCaseAndCategory_Id(String name, Integer categoryId, Pageable pageable);
    Page<Skill> findByCategory_Id(Integer categoryId, Pageable pageable);
    Optional<Skill> findByNameIgnoreCaseAndCategory_Id(String name, Integer categoryId);
}
