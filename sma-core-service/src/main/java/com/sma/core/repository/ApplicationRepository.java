package com.sma.core.repository;

import com.sma.core.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Integer> {
    boolean existsByResume_Id(Integer resumeId);
}
