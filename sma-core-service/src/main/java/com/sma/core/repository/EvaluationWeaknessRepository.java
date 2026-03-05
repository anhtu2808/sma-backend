package com.sma.core.repository;

import com.sma.core.entity.EvaluationWeakness;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EvaluationWeaknessRepository extends JpaRepository<EvaluationWeakness, Integer> {

    Optional<EvaluationWeakness> findByIdAndEvaluationId(Integer id, Integer evaluationId);

}
