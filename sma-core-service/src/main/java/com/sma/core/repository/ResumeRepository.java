package com.sma.core.repository;

import com.sma.core.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ResumeRepository extends JpaRepository<Resume, Integer>, JpaSpecificationExecutor<Resume> {
    boolean existsByRootResume_Id(Integer rootResumeId);
}
