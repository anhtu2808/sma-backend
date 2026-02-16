package com.sma.core.repository;

import com.sma.core.entity.Feature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, Integer> {
    java.util.List<Feature> findAllByIsActiveTrue();
    boolean existsByFeatureKey(String featureKey);
    boolean existsByNameIgnoreCase(String name);
    Optional<Feature> findByFeatureKeyAndIsActiveTrue(String featureKey);
}
