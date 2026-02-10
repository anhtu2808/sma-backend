package com.sma.core.service;

import com.sma.core.dto.request.planprice.PlanPriceRequest;
import com.sma.core.dto.response.planprice.PlanPriceResponse;

public interface PlanPriceService {
    PlanPriceResponse addPrice(Integer planId, PlanPriceRequest request);

    void deletePrice(Integer planId, Integer priceId);
}
