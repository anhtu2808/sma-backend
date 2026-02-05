package com.sma.core.controller.candidate;

import com.sma.core.dto.request.auth.GoogleTokenRequest;
import com.sma.core.dto.request.auth.RegisterRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.auth.AuthenticationResponse;
import com.sma.core.service.AuthService;
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
@RequestMapping("/v1/candidate/auth")
@RequiredArgsConstructor
public class CandidateAuthController {

    final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthenticationResponse> registerAsCandidate(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.<AuthenticationResponse>builder()
                .message("Register as candidate successfully")
                .data(authService.registerAsCandidate(request))
                .build();
    }

    @PostMapping("/google-login")
    public ApiResponse<AuthenticationResponse> loginWithGoogle(@RequestBody GoogleTokenRequest request) {
        return ApiResponse.<AuthenticationResponse>builder()
                .message("Login with google successfully")
                .data(authService.registerOrLogin(request.getIdToken()))
                .build();
    }

}
