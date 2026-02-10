package com.sma.core.controller;

import com.sma.core.dto.request.plan.PlanCreateRequest;
import com.sma.core.dto.request.plan.PlanFilterRequest;
import com.sma.core.dto.request.planprice.PlanPriceRequest;
import com.sma.core.dto.request.plan.PlanUpdateRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.planprice.PlanPriceResponse;
import com.sma.core.dto.response.plan.PlanResponse;
import com.sma.core.service.PlanService;
import com.sma.core.service.PlanPriceService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/plans")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlanController {

    PlanService planService;
    PlanPriceService planPriceService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<PlanResponse> create(@RequestBody @Valid PlanCreateRequest request) {
        return ApiResponse.<PlanResponse>builder()
                .data(planService.createPlan(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<PlanResponse> update(
            @PathVariable Integer id,
            @RequestBody @Valid PlanUpdateRequest request) {
        return ApiResponse.<PlanResponse>builder()
                .data(planService.updatePlan(id, request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<PlanResponse> getById(@PathVariable Integer id) {
        return ApiResponse.<PlanResponse>builder()
                .data(planService.getPlanById(id))
                .build();
    }

    @GetMapping
    public ApiResponse<PagingResponse<PlanResponse>> getAll(
            @ParameterObject PlanFilterRequest request
    ) {
        return ApiResponse.<PagingResponse<PlanResponse>>builder()
                .data(planService.getPlans(request))
                .build();
    }

    @PostMapping("/{planId}/prices")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<PlanPriceResponse> addPrice(
            @PathVariable Integer planId,
            @RequestBody @Valid PlanPriceRequest request
    ) {
        return ApiResponse.<PlanPriceResponse>builder()
                .data(planPriceService.addPrice(planId, request))
                .build();
    }

    @DeleteMapping("/{planId}/prices/{priceId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Void> deletePrice(
            @PathVariable Integer planId,
            @PathVariable Integer priceId
    ) {
        planPriceService.deletePrice(planId, priceId);
        return ApiResponse.<Void>builder()
                .message("Plan price deleted successfully")
                .build();
    }
}
