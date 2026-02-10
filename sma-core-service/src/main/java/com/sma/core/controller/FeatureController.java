package com.sma.core.controller;

import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.feature.FeatureResponse;
import com.sma.core.service.FeatureService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/features")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeatureController {

    FeatureService featureService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<List<FeatureResponse>> getAll(@RequestParam(required = false) Boolean onlyActive) {
        return ApiResponse.<List<FeatureResponse>>builder()
                .data(featureService.getAllFeatures(onlyActive))
                .build();
    }
}
