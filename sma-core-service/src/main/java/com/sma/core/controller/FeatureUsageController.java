package com.sma.core.controller;

import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.featureusage.FeatureUsageResponse;
import com.sma.core.service.FeatureUsageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/feature-usage")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeatureUsageController {

    FeatureUsageService featureUsageService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CANDIDATE','RECRUITER')")
    public ApiResponse<List<FeatureUsageResponse>> getCurrentUsage() {
        return ApiResponse.<List<FeatureUsageResponse>>builder()
                .data(featureUsageService.getCurrentUsage())
                .build();
    }
}
