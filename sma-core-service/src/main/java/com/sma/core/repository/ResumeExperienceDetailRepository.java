package com.sma.core.repository;

import com.sma.core.entity.ResumeExperienceDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeExperienceDetailRepository extends JpaRepository<ResumeExperienceDetail, Integer> {
    @Modifying
    @Query("delete from ResumeExperienceDetail red where red.experience.resume.id = :resumeId")
    void deleteByResumeId(@Param("resumeId") Integer resumeId);
}
