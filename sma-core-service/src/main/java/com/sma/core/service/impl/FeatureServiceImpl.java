package com.sma.core.service.impl;

import com.sma.core.dto.response.feature.FeatureResponse;
import com.sma.core.entity.Feature;
import com.sma.core.mapper.feature.FeatureMapper;
import com.sma.core.repository.FeatureRepository;
import com.sma.core.service.FeatureService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class FeatureServiceImpl implements FeatureService {

    FeatureRepository featureRepository;
    FeatureMapper featureMapper;

    @Override
    public List<FeatureResponse> getAllFeatures(Boolean onlyActive) {
        List<Feature> features = Boolean.TRUE.equals(onlyActive)
                ? featureRepository.findAllByIsActiveTrue()
                : featureRepository.findAll();
        return featureMapper.toResponses(features);
    }
}
