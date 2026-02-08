package com.sma.core.repository;

import com.sma.core.entity.ProjectSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectSkillRepository extends JpaRepository<ProjectSkill, Integer> {
    @Modifying
    @Query("delete from ProjectSkill ps where ps.project.resume.id = :resumeId")
    void deleteByResumeId(@Param("resumeId") Integer resumeId);

    Optional<ProjectSkill> findByIdAndProject_Resume_IdAndProject_Resume_Candidate_Id(Integer id, Integer resumeId, Integer candidateId);
}
