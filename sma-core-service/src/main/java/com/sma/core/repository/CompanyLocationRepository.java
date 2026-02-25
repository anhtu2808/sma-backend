package com.sma.core.repository;

import com.sma.core.entity.CompanyLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyLocationRepository extends JpaRepository<CompanyLocation, Integer> {

    Optional<CompanyLocation> findByIdAndCompanyId(Integer id, Integer companyId);
    List<CompanyLocation> findByCompanyId(Integer companyId);
}
