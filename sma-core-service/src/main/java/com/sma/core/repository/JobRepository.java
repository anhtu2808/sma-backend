package com.sma.core.repository;

import com.sma.core.entity.Job;
import com.sma.core.dto.response.job.JobStatusCountResponse;
import com.sma.core.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Integer>, JpaSpecificationExecutor<Job> {
    long countByCompanyId(Integer companyId);
    List<Job> findByExpDateBeforeAndStatus(LocalDateTime date, JobStatus status);

    @Query("""
            SELECT new com.sma.core.dto.response.job.JobStatusCountResponse(j.status, COUNT(j))
            FROM Job j
            WHERE j.company.id = :companyId
              AND (j.isSample = FALSE OR j.isSample IS NULL)
            GROUP BY j.status
            """)
    List<JobStatusCountResponse> countJobsByStatusByCompanyId(@Param("companyId") Integer companyId);
}
