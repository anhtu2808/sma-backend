package com.sma.core.service;

import com.sma.core.dto.request.usagelimit.UsageLimitRequest;
import com.sma.core.dto.response.usagelimit.UsageLimitResponse;

import java.util.List;

public interface UsageLimitService {
    UsageLimitResponse addLimit(Integer planId, UsageLimitRequest request);

    UsageLimitResponse updateLimit(Integer planId, Integer featureId, UsageLimitRequest request);

    void deleteLimit(Integer planId, Integer featureId);

    List<UsageLimitResponse> getLimits(Integer planId);
}
