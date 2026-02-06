package com.sma.core.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.sma.core.dto.request.auth.LoginRequest;
import com.sma.core.dto.request.auth.LogoutRequest;
import com.sma.core.dto.request.auth.RefreshTokenRequest;
import com.sma.core.dto.request.auth.RegisterRequest;
import com.sma.core.dto.response.auth.AuthenticationResponse;

public interface AuthService {

    AuthenticationResponse registerAsCandidate(RegisterRequest request);
    AuthenticationResponse login(LoginRequest request);
    AuthenticationResponse registerOrLogin(String idTokenString);
    Boolean logout(LogoutRequest request);
    AuthenticationResponse refreshToken(RefreshTokenRequest refreshToken);
}
