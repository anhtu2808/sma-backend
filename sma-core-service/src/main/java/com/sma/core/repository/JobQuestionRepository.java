package com.sma.core.repository;

import com.sma.core.entity.JobQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobQuestionRepository extends JpaRepository<JobQuestion, Integer> {
}
