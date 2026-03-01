package com.sma.core.repository;

import com.sma.core.entity.UsageEventContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsageEventContextRepository extends JpaRepository<UsageEventContext, Integer> {
}
