package com.sma.core.service;

import com.sma.core.dto.response.feature.FeatureResponse;
import com.sma.core.dto.request.feature.FeatureRequest;

import java.util.List;

public interface FeatureService {
    List<FeatureResponse> getAllFeatures(Boolean onlyActive);

    FeatureResponse createFeature(FeatureRequest request);

    FeatureResponse updateFeature(Integer id, FeatureRequest request);
}
