package com.sma.core.repository;

import com.sma.core.entity.Company;
import com.sma.core.enums.CompanyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer>, JpaSpecificationExecutor<Company> {
    boolean existsByEmail(String email);
    boolean existsByTaxIdentificationNumber(String taxIdentificationNumber);
    Page<Company> findByNameContainingIgnoreCaseAndStatus(String name, CompanyStatus status, Pageable pageable);
    Page<Company> findByStatus(CompanyStatus status, Pageable pageable);
    Page<Company> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
