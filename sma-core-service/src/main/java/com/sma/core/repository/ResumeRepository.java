package com.sma.core.repository;

import com.sma.core.entity.Resume;
import com.sma.core.enums.ResumeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Integer>, JpaSpecificationExecutor<Resume> {
    boolean existsByRootResume_Id(Integer rootResumeId);

    Optional<Resume> findByIdAndCandidate_Id(Integer id, Integer candidateId);

    Optional<Resume> findFirstByCandidate_IdAndTypeOrderByIdDesc(Integer candidateId, ResumeType type);

    @Query("""
            SELECT COUNT(r)
            FROM Resume r
            WHERE r.candidate.id = :candidateId
                AND r.type = :type
                AND (r.isDeleted = false OR r.isDeleted IS NULL)
            """)
    long countActiveByCandidateIdAndType(@Param("candidateId") Integer candidateId, @Param("type") ResumeType type);
}
