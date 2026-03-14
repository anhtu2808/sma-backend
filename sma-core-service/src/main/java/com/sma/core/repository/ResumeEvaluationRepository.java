package com.sma.core.repository;

import com.sma.core.entity.ResumeEvaluation;
import com.sma.core.enums.EvaluationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface ResumeEvaluationRepository extends JpaRepository<ResumeEvaluation, Integer> {

    Optional<ResumeEvaluation> findByResumeIdAndJobId(Integer resumeId, Integer jobId);

    Optional<ResumeEvaluation> findByResumeIdAndJobIdAndEvaluationType(Integer resumeId, Integer jobId, EvaluationType evaluationType);

    Page<ResumeEvaluation> findByJobId(Integer jobId, Pageable pageable);
    Set<ResumeEvaluation> findByJobIdAndResumeIdIn(Integer jobId, Set<Integer> resumeIds);
}
