package com.sma.core.repository;

import com.sma.core.entity.Resume;
import com.sma.core.enums.ResumeParseStatus;
import com.sma.core.enums.ResumeStatus;
import com.sma.core.enums.ResumeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Integer>, JpaSpecificationExecutor<Resume> {
    boolean existsByRootResume_Id(Integer rootResumeId);

    Optional<Resume> findByIdAndCandidate_Id(Integer id, Integer candidateId);

    Optional<Resume> findFirstByCandidate_IdAndTypeOrderByIdDesc(Integer candidateId, ResumeType type);

    @Modifying
    @Query("""
            UPDATE Resume r
            SET r.isDefault = false
            WHERE r.candidate.id = :candidateId
                AND (r.isDeleted = false OR r.isDeleted IS NULL)
            """)
    int clearDefaultByCandidateId(@Param("candidateId") Integer candidateId);

    @Query("""
            SELECT COUNT(r)
            FROM Resume r
            WHERE r.candidate.id = :candidateId
                AND r.type = :type
                AND (r.isDeleted = false OR r.isDeleted IS NULL)
            """)
    long countActiveByCandidateIdAndType(@Param("candidateId") Integer candidateId, @Param("type") ResumeType type);

    @Modifying
    @Query("""
            UPDATE Resume r
            SET r.parseStatus = :failStatus,
                r.status = :draftStatus,
                r.parseErrorMessage = :errorMessage,
                r.parseUpdatedAt = :updatedAt
            WHERE r.parseStatus = :partialStatus
                AND r.parseRequestedAt IS NOT NULL
                AND r.parseRequestedAt <= :deadline
            """)
    int markTimedOutParses(
            @Param("partialStatus") ResumeParseStatus partialStatus,
            @Param("failStatus") ResumeParseStatus failStatus,
            @Param("draftStatus") ResumeStatus draftStatus,
            @Param("errorMessage") String errorMessage,
            @Param("updatedAt") LocalDateTime updatedAt,
            @Param("deadline") LocalDateTime deadline
    );
}
