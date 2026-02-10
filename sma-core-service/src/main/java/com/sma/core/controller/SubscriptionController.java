package com.sma.core.controller;

import com.sma.core.dto.request.subscription.CreateSubscriptionRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.service.SubscriptionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    SubscriptionService subscriptionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('RECRUITER, CANDIDATE')")
    public ApiResponse<String> createSubscription(@RequestBody CreateSubscriptionRequest request){
        return ApiResponse.<String>builder()
                .message("Create subscription successfully")
                .data(subscriptionService.createSubscription(request))
                .build();
    }

}
