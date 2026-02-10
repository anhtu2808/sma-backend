package com.sma.core.service;

import com.sma.core.dto.request.plan.PlanCreateRequest;
import com.sma.core.dto.request.plan.PlanFilterRequest;
import com.sma.core.dto.request.plan.PlanUpdateRequest;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.plan.PlanResponse;

public interface PlanService {
    PlanResponse createPlan(PlanCreateRequest request);

    PlanResponse updatePlan(Integer id, PlanUpdateRequest request);

    PlanResponse getPlanById(Integer id);

    PagingResponse<PlanResponse> getPlans(PlanFilterRequest request);
}
