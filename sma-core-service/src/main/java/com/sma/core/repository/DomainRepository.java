package com.sma.core.repository;

import com.sma.core.entity.Domain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DomainRepository extends JpaRepository<Domain, Integer> {
    Page<Domain> findByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByName(String name);
}
