package com.sma.core.repository;

import com.sma.core.entity.ExperienceSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExperienceSkillRepository extends JpaRepository<ExperienceSkill, Integer> {
    @Modifying
    @Query("DELETE FROM ExperienceSkill es WHERE es.detail.experience.resume.id = :resumeId")
    void deleteByResumeId(@Param("resumeId") Integer resumeId);

    Optional<ExperienceSkill> findByIdAndDetail_Experience_Resume_IdAndDetail_Experience_Resume_Candidate_Id(Integer id, Integer resumeId, Integer candidateId);
}
