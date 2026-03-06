package com.sma.core.controller;

import com.sma.core.dto.request.company.BlockCandidateRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.company.BlockedCandidateResponse;
import com.sma.core.service.CompanyBlockedCandidateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/company/blacklist")
@RequiredArgsConstructor
public class CompanyBlacklistController {
    private final CompanyBlockedCandidateService blacklistService;

    @PostMapping
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<Void> block(@RequestBody @Valid BlockCandidateRequest request) {
        blacklistService.blockCandidate(request);
        return ApiResponse.<Void>builder()
                .message("Blocked candidate successfully")
                .build();
    }

    @DeleteMapping("/{candidateId}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<Void> unblock(@PathVariable Integer candidateId) {
        blacklistService.unblockCandidate(candidateId);
        return ApiResponse.<Void>builder()
                .message("Unblocked candidate successfully")
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<PagingResponse<BlockedCandidateResponse>> getBlacklist(
            @RequestParam(required = false) String keyword,
            @ParameterObject Pageable pageable) {

        return ApiResponse.<PagingResponse<BlockedCandidateResponse>>builder()
                .message("Get blacklist successfully")
                .data(blacklistService.getBlacklist(pageable, keyword))
                .build();
    }
}
