package com.sma.core.repository;

import com.sma.core.entity.EvaluationExperienceDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationExperienceDetailRepository extends JpaRepository<EvaluationExperienceDetail, Integer> {
}
