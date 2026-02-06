package com.sma.core.repository;

import com.sma.core.entity.ScoringCriteria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface ScoringCriteriaRepository extends JpaRepository<ScoringCriteria, Integer> {

    Optional<ScoringCriteria> findByCriteria_IdAndJob_Id(Integer criteriaId, Integer jobId);

}
