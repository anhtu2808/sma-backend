package com.sma.core.repository;

import com.sma.core.entity.ProposedResume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ProposedResumeRepository extends JpaRepository<ProposedResume, Integer> {

    Page<ProposedResume> findByJobId(Integer jobId, Pageable pageable);

}
