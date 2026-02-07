package com.sma.core.controller.recruiter;

import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.myinfo.RecruiterMyInfoResponse;
import com.sma.core.service.RecruiterService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/recruiter")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecruiterController {

    RecruiterService recruiterService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<RecruiterMyInfoResponse> getMyInfo() {
        return ApiResponse.<RecruiterMyInfoResponse>builder()
                .message("Get recruiter my info successfully")
                .data(recruiterService.getMyInfo())
                .build();
    }
}
