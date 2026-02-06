package com.sma.core.repository;

import com.sma.core.entity.ResumeCertification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeCertificationRepository extends JpaRepository<ResumeCertification, Integer> {
    @Modifying
    @Query("delete from ResumeCertification rc where rc.resume.id = :resumeId")
    void deleteByResumeId(@Param("resumeId") Integer resumeId);
}
