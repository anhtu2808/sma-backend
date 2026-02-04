package com.sma.core.repository;

import com.sma.core.entity.Job;
import com.sma.core.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Integer>, JpaSpecificationExecutor<Job> {

    List<Job> findByExpDateBeforeAndStatus(LocalDateTime date, JobStatus status);

}
