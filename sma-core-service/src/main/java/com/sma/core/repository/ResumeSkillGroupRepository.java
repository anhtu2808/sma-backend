package com.sma.core.repository;

import com.sma.core.entity.ResumeSkillGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeSkillGroupRepository extends JpaRepository<ResumeSkillGroup, Integer> {

    @Modifying
    @Query("delete from ResumeSkillGroup rsg where rsg.resume.id = :resumeId")
    void deleteByResumeId(@Param("resumeId") Integer resumeId);

    @Query("""
            SELECT rsg
            FROM ResumeSkillGroup rsg
            WHERE rsg.resume.id = :resumeId
              AND LOWER(FUNCTION('REGEXP_REPLACE', TRIM(rsg.name), '\\s+', ' ', 'g'))
                    = LOWER(FUNCTION('REGEXP_REPLACE', TRIM(:name), '\\s+', ' ', 'g'))
            ORDER BY rsg.id ASC
            """)
    List<ResumeSkillGroup> findByResumeIdAndNormalizedName(@Param("resumeId") Integer resumeId, @Param("name") String name);

    @Query("select coalesce(max(rsg.orderIndex), 0) from ResumeSkillGroup rsg where rsg.resume.id = :resumeId")
    Integer findMaxOrderIndexByResumeId(@Param("resumeId") Integer resumeId);
}
