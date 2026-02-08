package com.sma.core.repository;

import com.sma.core.entity.Resume;
import com.sma.core.enums.ResumeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Integer>, JpaSpecificationExecutor<Resume> {
    boolean existsByRootResume_Id(Integer rootResumeId);

    Optional<Resume> findFirstByCandidate_IdAndTypeOrderByIdDesc(Integer candidateId, ResumeType type);
}
