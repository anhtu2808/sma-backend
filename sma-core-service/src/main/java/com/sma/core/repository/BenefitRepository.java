package com.sma.core.repository;

import com.sma.core.entity.Benefit;
import com.sma.core.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BenefitRepository  extends JpaRepository<Benefit, Integer> {
}
