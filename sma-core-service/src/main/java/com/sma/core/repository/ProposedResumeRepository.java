package com.sma.core.repository;

import com.sma.core.entity.ProposedResume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProposedResumeRepository extends JpaRepository<ProposedResume, Integer> {

    Page<ProposedResume> findByJobId(Integer jobId, Pageable pageable);
    boolean existsByJobIdAndResumeId(Integer jobId, Integer resumeId);
    Optional<ProposedResume> findByJobIdAndResumeId(Integer jobId, Integer resumeId);
    List<ProposedResume> findByJobIdAndResumeIdIn(Integer jobId, Set<Integer> resumeIds);
}
