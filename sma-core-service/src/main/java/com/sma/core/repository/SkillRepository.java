package com.sma.core.repository;

import com.sma.core.entity.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Query("""
            SELECT s
            FROM Skill s
            WHERE LOWER(FUNCTION('REGEXP_REPLACE', TRIM(s.name), '\\s+', ' ', 'g'))
                = LOWER(FUNCTION('REGEXP_REPLACE', TRIM(:name), '\\s+', ' ', 'g'))
            ORDER BY s.id ASC
            """)
    List<Skill> findAllByNormalizedName(@Param("name") String name);

    @Query("""
            SELECT s
            FROM Skill s
            WHERE s.category.id = :categoryId
              AND LOWER(FUNCTION('REGEXP_REPLACE', TRIM(s.name), '\\s+', ' ', 'g'))
                  = LOWER(FUNCTION('REGEXP_REPLACE', TRIM(:name), '\\s+', ' ', 'g'))
            ORDER BY s.id ASC
            """)
    List<Skill> findAllByNormalizedNameAndCategoryId(
            @Param("name") String name,
            @Param("categoryId") Integer categoryId
    );
}
