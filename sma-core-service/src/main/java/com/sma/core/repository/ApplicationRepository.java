package com.sma.core.repository;

import com.sma.core.entity.Application;
import com.sma.core.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Integer> {
    boolean existsByResume_Id(Integer resumeId);

    long countByCandidateIdAndJobId(Integer candidateId, Integer jobId);

    List<Application> findByCandidateIdAndJobIdOrderByAppliedAtDesc(Integer candidateId, Integer jobId);

    @Query("SELECT COUNT(a) > 0 FROM Application a " +
            "WHERE a.candidate.id = :candidateId " +
            "AND a.job.id = :jobId " +
            "AND a.status IN (:rejectStatuses)")
    boolean hasBeenRejected(@Param("candidateId") Integer candidateId,
                            @Param("jobId") Integer jobId,
                            @Param("rejectStatuses") List<ApplicationStatus> rejectStatuses);

    Optional<Application> findFirstByCandidateIdAndJobIdOrderByAppliedAtDesc(Integer candidateId, Integer jobId);

    Optional<Application> findTopByCandidate_IdAndJob_IdOrderByAttemptDesc(Integer candidateId, Integer jobId);
}
