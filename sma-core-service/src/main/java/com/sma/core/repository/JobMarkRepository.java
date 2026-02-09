package com.sma.core.repository;

import com.sma.core.entity.JobMark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobMarkRepository extends JpaRepository<JobMark, Integer> {

    Page<JobMark> findByUser_Id(Integer userId, Pageable pageable);
    Optional<JobMark> findByUser_IdAndJob_Id(Integer userId, Integer jobId);

}
