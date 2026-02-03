package com.sma.core.controller;

import com.sma.core.dto.request.auth.LoginRequest;
import com.sma.core.dto.request.auth.RecruiterRegisterRequest;
import com.sma.core.dto.request.auth.LogoutRequest;
import com.sma.core.dto.request.auth.RefreshTokenRequest;
import com.sma.core.dto.request.auth.RegisterRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.auth.AuthenticationResponse;
import com.sma.core.service.AuthService;
import com.sma.core.service.RecruiterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController {

    final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.<AuthenticationResponse>builder()
                .message("Login successfully")
                .data(authService.login(request))
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Boolean> logout(@RequestBody LogoutRequest request) {
        return ApiResponse.<Boolean>builder()
                .message("Logout successfully")
                .data(authService.logout(request))
                .build();
    }

    @PostMapping("/refresh-token")
    public ApiResponse<AuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ApiResponse.<AuthenticationResponse>builder()
                .message("Refresh token successfully")
                .data(authService.refreshToken(request))
                .build();
    }

}
