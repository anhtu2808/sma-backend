package com.sma.core.repository;

import com.sma.core.entity.EvaluationCriteriaSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationCriteriaSuggestionRepository extends JpaRepository<EvaluationCriteriaSuggestion, Integer> {
}
