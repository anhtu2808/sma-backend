package com.sma.core.repository;

import com.sma.core.entity.ResumeProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeProjectRepository extends JpaRepository<ResumeProject, Integer> {
    @Modifying
    @Query("delete from ResumeProject rp where rp.resume.id = :resumeId")
    void deleteByResumeId(@Param("resumeId") Integer resumeId);
}
