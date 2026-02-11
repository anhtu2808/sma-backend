package com.sma.core.repository;

import com.sma.core.entity.BannedKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannedKeywordRepository extends JpaRepository<BannedKeyword, Integer> {
    List<BannedKeyword> findByIsActiveTrue();
}
