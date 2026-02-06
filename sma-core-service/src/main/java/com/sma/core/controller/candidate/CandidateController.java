package com.sma.core.controller.candidate;

import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.myinfo.CandidateMyInfoResponse;
import com.sma.core.service.CandidateService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/candidate")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CandidateController {

    CandidateService candidateService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<CandidateMyInfoResponse> getMyInfo() {
        return ApiResponse.<CandidateMyInfoResponse>builder()
                .message("Get candidate my info successfully")
                .data(candidateService.getMyInfo())
                .build();
    }
}
