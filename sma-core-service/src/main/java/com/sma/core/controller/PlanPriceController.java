package com.sma.core.controller;

import com.sma.core.dto.request.planprice.PlanPriceRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.planprice.PlanPriceResponse;
import com.sma.core.service.PlanPriceService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/plans")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlanPriceController {

    PlanPriceService planPriceService;

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

    @GetMapping("/{planId}/prices")
    public ApiResponse<List<PlanPriceResponse>> getPrices(@PathVariable Integer planId) {
        return ApiResponse.<List<PlanPriceResponse>>builder()
                .data(planPriceService.getPrices(planId))
                .build();
    }

    @PutMapping("/{planId}/prices/{priceId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<PlanPriceResponse> updatePrice(
            @PathVariable Integer planId,
            @PathVariable Integer priceId,
            @RequestBody @Valid PlanPriceRequest request
    ) {
        return ApiResponse.<PlanPriceResponse>builder()
                .data(planPriceService.updatePrice(planId, priceId, request))
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
