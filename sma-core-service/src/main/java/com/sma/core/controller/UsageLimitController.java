package com.sma.core.controller;

import com.sma.core.dto.request.usagelimit.UsageLimitRequest;
import com.sma.core.dto.request.usagelimit.UsageLimitUpdateRequest;
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
@RequestMapping("/v1/usage-limits")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UsageLimitController {

    UsageLimitService usageLimitService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<UsageLimitResponse> addLimit(
            @RequestBody @Valid UsageLimitRequest request
    ) {
        return ApiResponse.<UsageLimitResponse>builder()
                .data(usageLimitService.addLimit(request.getPlanId(), request))
                .build();
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<UsageLimitResponse> updateLimit(
            @RequestParam Integer planId,
            @RequestParam Integer featureId,
            @RequestBody @Valid UsageLimitUpdateRequest request
    ) {
        return ApiResponse.<UsageLimitResponse>builder()
                .data(usageLimitService.updateLimit(planId, featureId, request))
                .build();
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Void> deleteLimit(
            @RequestParam Integer planId,
            @RequestParam Integer featureId
    ) {
        usageLimitService.deleteLimit(planId, featureId);
        return ApiResponse.<Void>builder()
                .message("Usage limit deleted successfully")
                .build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<List<UsageLimitResponse>> getLimits(@RequestParam Integer planId) {
        return ApiResponse.<List<UsageLimitResponse>>builder()
                .data(usageLimitService.getLimits(planId))
                .build();
    }
}
