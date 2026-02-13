package com.sma.core.repository;

import com.sma.core.entity.ResumeProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeProjectRepository extends JpaRepository<ResumeProject, Integer> {
    @Modifying
    @Query("DELETE FROM ResumeProject rp WHERE rp.resume.id = :resumeId")
    void deleteByResumeId(@Param("resumeId") Integer resumeId);

    Optional<ResumeProject> findByIdAndResume_IdAndResume_Candidate_Id(Integer id, Integer resumeId, Integer candidateId);

    @Query("SELECT COALESCE(MAX(rp.orderIndex), 0) FROM ResumeProject rp WHERE rp.resume.id = :resumeId")
    Integer findMaxOrderIndexByResumeId(@Param("resumeId") Integer resumeId);
}
