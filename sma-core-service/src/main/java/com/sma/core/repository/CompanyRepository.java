package com.sma.core.repository;

import com.sma.core.entity.Company;
import com.sma.core.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer>, JpaSpecificationExecutor<Company> {
    boolean existsByEmail(String email);
    boolean existsByTaxIdentificationNumber(String taxIdentificationNumber);
}
