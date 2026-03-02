package com.sma.core.repository;

import com.sma.core.entity.ResumeEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeEvaluationRepository extends JpaRepository<ResumeEvaluation, Integer> {
}
