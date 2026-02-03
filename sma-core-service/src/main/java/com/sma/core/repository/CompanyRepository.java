package com.sma.core.repository;

import com.sma.core.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> {
    boolean existsByEmail(String email);
    boolean existsByTaxIdentificationNumber(String taxIdentificationNumber);
}
