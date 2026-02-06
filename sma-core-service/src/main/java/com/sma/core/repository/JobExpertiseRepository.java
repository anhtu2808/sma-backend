package com.sma.core.repository;

import com.sma.core.entity.JobExpertise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobExpertiseRepository extends JpaRepository<JobExpertise, Integer> {

}
