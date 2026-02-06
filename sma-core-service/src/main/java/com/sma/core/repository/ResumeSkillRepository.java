package com.sma.core.repository;

import com.sma.core.entity.ResumeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeSkillRepository extends JpaRepository<ResumeSkill, Integer> {
    @Modifying
    @Query("delete from ResumeSkill rs where rs.resume.id = :resumeId")
    void deleteByResumeId(@Param("resumeId") Integer resumeId);
}
