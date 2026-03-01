package com.sma.core.service;

import com.sma.core.dto.request.usage.UsageHistoryFilterRequest;
import com.sma.core.dto.response.featureusage.FeatureUsageResponse;
import com.sma.core.dto.response.usage.UsageEventResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UsageService {
    List<FeatureUsageResponse> getCurrentUsage();

    Page<UsageEventResponse> getUsageHistory(UsageHistoryFilterRequest request);
}
