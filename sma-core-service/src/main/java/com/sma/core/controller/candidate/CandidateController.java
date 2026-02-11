package com.sma.core.controller.candidate;

import com.sma.core.dto.request.candidate.UpdateCandidateProfileRequest;
import com.sma.core.dto.request.subscription.CreateSubscriptionRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.candidate.CandidateProfileResponse;
import com.sma.core.dto.response.myinfo.CandidateMyInfoResponse;
import com.sma.core.enums.Role;
import com.sma.core.service.CandidateService;
import com.sma.core.service.SubscriptionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/candidate")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CandidateController {

    CandidateService candidateService;
    SubscriptionService subscriptionService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<CandidateMyInfoResponse> getMyInfo() {
        return ApiResponse.<CandidateMyInfoResponse>builder()
                .message("Get candidate my info successfully")
                .data(candidateService.getMyInfo())
                .build();
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<CandidateProfileResponse> getMyProfile() {
        return ApiResponse.<CandidateProfileResponse>builder()
                .message("Get candidate profile successfully")
                .data(candidateService.getMyProfile())
                .build();
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<CandidateProfileResponse> updateMyProfile(@RequestBody UpdateCandidateProfileRequest request) {
        return ApiResponse.<CandidateProfileResponse>builder()
                .message("Update candidate profile successfully")
                .data(candidateService.updateMyProfile(request))
                .build();
    }

    @PostMapping("/{candidateId}/subscription")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> createCandidateSubscription(@RequestBody CreateSubscriptionRequest request,
                                                         @PathVariable Integer candidateId){
        return ApiResponse.<String>builder()
                .message("Create candidate subscription successfully")
                .data(subscriptionService.createSubscription(candidateId, request, Role.CANDIDATE))
                .build();
    }
}
