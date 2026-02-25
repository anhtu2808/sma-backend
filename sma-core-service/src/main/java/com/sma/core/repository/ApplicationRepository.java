package com.sma.core.repository;

import com.sma.core.entity.Application;
import com.sma.core.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @EntityGraph(attributePaths = {
            "resume",
            "resume.candidate",
            "resume.experiences.details.skills",
            "resume.skillGroups.skills.skill",
            "resume.evaluations.criteriaScores.scoringCriteria.criteria",
            "resume.evaluations.gaps",
            "resume.evaluations.weaknesses"
    })
    Optional<Application> findById(Integer id);

    @EntityGraph(attributePaths = {"resume", "resume.experiences", "resume.skillGroups", "resume.evaluations"})
    Page<Application> findAll(Specification<Application> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"job", "resume"})
    List<Application> findByCandidate_Id(Integer candidateId);
}
