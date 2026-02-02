package com.sma.core.controller;

import com.sma.core.dto.request.auth.LoginRequest;
import com.sma.core.dto.request.auth.LogoutRequest;
import com.sma.core.dto.request.auth.RegisterRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.auth.AuthenticationResponse;
import com.sma.core.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/v1/auth")
@Tag(name = "Authentication", description = "APIs for user authentication including registration, login, and logout")
public class AuthController {

    @Autowired
    AuthService authService;

    @Operation(
            summary = "Register as candidate",
            description = "Register a new candidate account with email, password, gender, and full name. Returns authentication tokens upon successful registration.")
    @PostMapping("/candidate/register")
    public ApiResponse<AuthenticationResponse> registerAsCandidate(@RequestBody RegisterRequest request) {
        return ApiResponse.<AuthenticationResponse>builder()
                .message("Register as candidate successfully")
                .data(authService.registerAsCandidate(request))
                .build();
    }

    @Operation(
            summary = "Login",
            description = "Authenticate user with email and password. Returns access token and refresh token upon successful authentication.")
    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.<AuthenticationResponse>builder()
                .message("Login successfully")
                .data(authService.login(request))
                .build();
    }

    @Operation(
            summary = "Login with Google",
            description = "Authenticate user using Google OAuth2.0 ID token. If the user doesn't exist, a new account will be created automatically. Returns authentication tokens upon successful login.")
    @PostMapping("/google-login")
    public ApiResponse<AuthenticationResponse> loginWithGoogle(@RequestParam("idToken") String idToken) {
        var googlePayload = authService.verifyGoogleIdToken(idToken);
        var email = googlePayload.getEmail();
        return ApiResponse.<AuthenticationResponse>builder()
                .message("Login successfully")
                .data(authService.registerOrLogin(email))
                .build();
    }

    @Operation(
            summary = "Logout",
            description = "Invalidate the user's refresh token to end the session. The refresh token will be blacklisted and cannot be used again.")
    @PostMapping("/logout")
    public ApiResponse<Boolean> logout(@RequestBody LogoutRequest request) {
        return ApiResponse.<Boolean>builder()
                .message("Logout successfully")
                .data(authService.logout(request))
                .build();
    }

}
