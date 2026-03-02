package com.sma.core.repository;

import com.sma.core.entity.EvaluationGap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationGapRepository extends JpaRepository<EvaluationGap, Integer> {
}
