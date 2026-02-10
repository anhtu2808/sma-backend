package com.sma.core.controller;

import com.sma.core.dto.request.usagelimit.UsageLimitRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.usagelimit.UsageLimitResponse;
import com.sma.core.service.UsageLimitService;
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
public class UsageLimitController {

    UsageLimitService usageLimitService;

    @PostMapping("/{planId}/usage-limits")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<UsageLimitResponse> addLimit(
            @PathVariable Integer planId,
            @RequestBody @Valid UsageLimitRequest request
    ) {
        return ApiResponse.<UsageLimitResponse>builder()
                .data(usageLimitService.addLimit(planId, request))
                .build();
    }

    @PutMapping("/{planId}/usage-limits/{featureId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<UsageLimitResponse> updateLimit(
            @PathVariable Integer planId,
            @PathVariable Integer featureId,
            @RequestBody @Valid UsageLimitRequest request
    ) {
        return ApiResponse.<UsageLimitResponse>builder()
                .data(usageLimitService.updateLimit(planId, featureId, request))
                .build();
    }

    @DeleteMapping("/{planId}/usage-limits/{featureId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Void> deleteLimit(
            @PathVariable Integer planId,
            @PathVariable Integer featureId
    ) {
        usageLimitService.deleteLimit(planId, featureId);
        return ApiResponse.<Void>builder()
                .message("Usage limit deleted successfully")
                .build();
    }

    @GetMapping("/{planId}/usage-limits")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<List<UsageLimitResponse>> getLimits(@PathVariable Integer planId) {
        return ApiResponse.<List<UsageLimitResponse>>builder()
                .data(usageLimitService.getLimits(planId))
                .build();
    }
}
