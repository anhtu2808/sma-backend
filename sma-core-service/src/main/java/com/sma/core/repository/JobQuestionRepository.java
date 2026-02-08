package com.sma.core.repository;

import com.sma.core.entity.JobQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface JobQuestionRepository
        extends JpaRepository<JobQuestion, Integer>, JpaSpecificationExecutor<JobQuestion> {
    Page<JobQuestion> findByDeletedFalse(Pageable pageable);

    Page<JobQuestion> findByJobIdAndDeletedFalse(Integer jobId, Pageable pageable);
}
