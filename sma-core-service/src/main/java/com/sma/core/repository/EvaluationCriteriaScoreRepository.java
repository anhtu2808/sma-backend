package com.sma.core.repository;

import com.sma.core.entity.EvaluationCriteriaScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationCriteriaScoreRepository extends JpaRepository<EvaluationCriteriaScore, Integer> {
}
