package com.sma.core.repository;

import com.sma.core.entity.ResumeEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeEducationRepository extends JpaRepository<ResumeEducation, Integer> {
    @Modifying
    @Query("delete from ResumeEducation re where re.resume.id = :resumeId")
    void deleteByResumeId(@Param("resumeId") Integer resumeId);
}
