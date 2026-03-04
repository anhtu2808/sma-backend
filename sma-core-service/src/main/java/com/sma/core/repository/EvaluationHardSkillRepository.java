package com.sma.core.repository;

import com.sma.core.entity.EvaluationHardSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationHardSkillRepository extends JpaRepository<EvaluationHardSkill, Integer> {
}
