package com.sma.core.repository;

import com.sma.core.entity.SkillCategory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillCategoryRepository extends JpaRepository<SkillCategory, Integer> {
    boolean existsByName(String name);
    Page<SkillCategory> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Optional<SkillCategory> findByNameIgnoreCase(String name);

    @Query("""
            SELECT sc
            FROM SkillCategory sc
            WHERE LOWER(FUNCTION('REGEXP_REPLACE', TRIM(sc.name), '\\s+', ' ', 'g'))
                = LOWER(FUNCTION('REGEXP_REPLACE', TRIM(:name), '\\s+', ' ', 'g'))
            ORDER BY sc.id ASC
            """)
    List<SkillCategory> findAllByNormalizedName(@Param("name") String name);
}
