package com.sma.core.repository;

import com.sma.core.entity.ResumeExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeExperienceRepository extends JpaRepository<ResumeExperience, Integer> {
    @Modifying
    @Query("delete from ResumeExperience re where re.resume.id = :resumeId")
    void deleteByResumeId(@Param("resumeId") Integer resumeId);

    Optional<ResumeExperience> findByIdAndResume_IdAndResume_Candidate_Id(Integer id, Integer resumeId, Integer candidateId);
}
