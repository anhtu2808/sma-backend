package com.sma.core.repository;

import com.sma.core.entity.ResumeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeSkillRepository extends JpaRepository<ResumeSkill, Integer> {
    @Modifying
    @Query("delete from ResumeSkill rs where rs.skillGroup.resume.id = :resumeId")
    void deleteByResumeId(@Param("resumeId") Integer resumeId);

    Optional<ResumeSkill> findByIdAndSkillGroup_Resume_IdAndSkillGroup_Resume_Candidate_Id(Integer id, Integer resumeId, Integer candidateId);

    boolean existsBySkillGroup_Id(Integer skillGroupId);
}
