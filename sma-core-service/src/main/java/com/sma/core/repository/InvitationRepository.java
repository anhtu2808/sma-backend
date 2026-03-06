package com.sma.core.repository;

import com.sma.core.entity.Invitation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Integer> {

    Page<Invitation> findByCandidateId(Integer candidateId, Pageable pageable);
    Page<Invitation> findByCompanyId(Integer companyId, Pageable pageable);

}
