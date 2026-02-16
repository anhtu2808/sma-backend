package com.sma.core.repository;

import com.sma.core.entity.ResumeEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeEducationRepository extends JpaRepository<ResumeEducation, Integer> {
    @Modifying
    @Query("DELETE FROM ResumeEducation re WHERE re.resume.id = :resumeId")
    void deleteByResumeId(@Param("resumeId") Integer resumeId);

    Optional<ResumeEducation> findByIdAndResume_IdAndResume_Candidate_Id(Integer id, Integer resumeId, Integer candidateId);

    @Query("SELECT COALESCE(MAX(re.orderIndex), 0) FROM ResumeEducation re WHERE re.resume.id = :resumeId")
    Integer findMaxOrderIndexByResumeId(@Param("resumeId") Integer resumeId);
}
