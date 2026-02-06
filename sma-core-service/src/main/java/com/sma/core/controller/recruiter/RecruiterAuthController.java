package com.sma.core.controller.recruiter;

import com.sma.core.dto.request.auth.RecruiterRegisterRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.service.RecruiterService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/v1/recruiter/auth")
@RequiredArgsConstructor
public class RecruiterAuthController {

    final RecruiterService recruiterService;

    //Register as recruiter
    @PostMapping("/register")
    public ApiResponse<Void> registerAsRecruiter(
            @Valid @RequestBody RecruiterRegisterRequest request
    ) {
        recruiterService.registerRecruiter(request);
        return ApiResponse.<Void>builder()
                .message("Registration submitted successfully. Please wait for admin approval.")
                .build();
    }
}
