package com.sma.core.service.impl;

import com.sma.core.dto.request.feature.FeatureRequest;
import com.sma.core.dto.response.feature.FeatureResponse;
import com.sma.core.entity.Feature;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
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
@Transactional
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

    @Override
    public FeatureResponse createFeature(FeatureRequest request) {
        String featureKey = request.getFeatureKey().name();
        if (featureRepository.existsByFeatureKey(featureKey)) {
            throw new AppException(ErrorCode.FEATURE_KEY_EXISTS);
        }
        if (featureRepository.existsByNameIgnoreCase(request.getName())) {
            throw new AppException(ErrorCode.FEATURE_NAME_EXISTS);
        }
        Feature feature = featureMapper.toEntity(request);
        if (feature.getIsActive() == null) {
            feature.setIsActive(true);
        }
        return featureMapper.toResponse(featureRepository.save(feature));
    }

    @Override
    public FeatureResponse updateFeature(Integer id, FeatureRequest request) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.FEATURE_NOT_FOUND));

        String featureKey = request.getFeatureKey().name();
        if (!featureKey.equals(feature.getFeatureKey())
                && featureRepository.existsByFeatureKey(featureKey)) {
            throw new AppException(ErrorCode.FEATURE_KEY_EXISTS);
        }
        if (!request.getName().equalsIgnoreCase(feature.getName())
                && featureRepository.existsByNameIgnoreCase(request.getName())) {
            throw new AppException(ErrorCode.FEATURE_NAME_EXISTS);
        }

        featureMapper.updateFromRequest(request, feature);
        if (feature.getIsActive() == null) {
            feature.setIsActive(true);
        }
        return featureMapper.toResponse(featureRepository.save(feature));
    }
}
