package com.sma.core.controller;


import com.sma.core.dto.request.auth.LoginRequest;
import com.sma.core.dto.request.auth.RegisterRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.auth.AuthenticationResponse;
import com.sma.core.service.AuthService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/v1/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    /**
     * Register as candidate
     */
    @PostMapping("/candidate/register")
    public ApiResponse<AuthenticationResponse> registerAsCandidate(@RequestBody RegisterRequest request) {
        return ApiResponse.<AuthenticationResponse>builder()
                .message("Register as candidate successfully")
                .data(authService.registerAsCandidate(request))
                .build();
    }

    /**
     * Login
     */
    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.<AuthenticationResponse>builder()
                .message("Login successfully")
                .data(authService.login(request))
                .build();
    }

    /**
     * Login with Google
     */
    @PostMapping("/google-login")
    public ApiResponse<AuthenticationResponse> loginWithGoogle(@RequestParam("idToken") String idToken) {
        var googlePayload = authService.verifyGoogleIdToken(idToken);
        var email = googlePayload.getEmail();
        return ApiResponse.<AuthenticationResponse>builder()
                .message("Login successfully")
                .data(authService.registerOrLogin(email))
                .build();
    }

}
