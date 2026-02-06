package com.sma.core.repository;

import com.sma.core.entity.ExperienceSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExperienceSkillRepository extends JpaRepository<ExperienceSkill, Integer> {
    @Modifying
    @Query("delete from ExperienceSkill es where es.detail.experience.resume.id = :resumeId")
    void deleteByResumeId(@Param("resumeId") Integer resumeId);
}
