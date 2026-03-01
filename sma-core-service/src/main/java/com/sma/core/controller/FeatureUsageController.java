package com.sma.core.controller;

import com.sma.core.dto.request.usage.UsageHistoryFilterRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.featureusage.FeatureUsageResponse;
import com.sma.core.dto.response.usage.UsageEventResponse;
import com.sma.core.service.UsageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
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

    UsageService usageService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CANDIDATE','RECRUITER')")
    public ApiResponse<List<FeatureUsageResponse>> getCurrentUsage() {
        return ApiResponse.<List<FeatureUsageResponse>>builder()
                .data(usageService.getCurrentUsage())
                .build();
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('CANDIDATE','RECRUITER')")
    public ApiResponse<PagingResponse<UsageEventResponse>> getUsageHistory(
            @ParameterObject UsageHistoryFilterRequest request) {
        Page<UsageEventResponse> page = usageService.getUsageHistory(request);

        PagingResponse<UsageEventResponse> pagingResponse = PagingResponse.fromPage(page);

        return ApiResponse.<PagingResponse<UsageEventResponse>>builder()
                .data(pagingResponse)
                .build();
    }
}
