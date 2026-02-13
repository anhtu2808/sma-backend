package com.sma.core.service;

import com.sma.core.dto.response.featureusage.FeatureUsageResponse;

import java.util.List;

public interface FeatureUsageService {
    List<FeatureUsageResponse> getCurrentUsage();
}
