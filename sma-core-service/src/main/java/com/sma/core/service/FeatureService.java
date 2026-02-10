package com.sma.core.service;

import com.sma.core.dto.response.feature.FeatureResponse;

import java.util.List;

public interface FeatureService {
    List<FeatureResponse> getAllFeatures(Boolean onlyActive);
}
