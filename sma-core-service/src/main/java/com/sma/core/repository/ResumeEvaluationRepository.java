package com.sma.core.repository;

import com.sma.core.entity.ResumeEvaluation;
import com.sma.core.enums.EvaluationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeEvaluationRepository extends JpaRepository<ResumeEvaluation, Integer> {

    Optional<ResumeEvaluation> findByResumeIdAndJobId(Integer resumeId, Integer jobId);

    Optional<ResumeEvaluation> findByResumeIdAndJobIdAndEvaluationType(Integer resumeId, Integer jobId, EvaluationType evaluationType);

}
