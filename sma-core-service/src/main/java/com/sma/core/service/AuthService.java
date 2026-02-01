package com.sma.core.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.sma.core.dto.request.auth.AuthenticationRequest;
import com.sma.core.dto.response.auth.AuthenticationResponse;

public interface AuthService {

    GoogleIdToken.Payload verifyGoogleIdToken(String idTokenString);
    AuthenticationResponse registerAsCandidate(AuthenticationRequest request);
    AuthenticationResponse login(AuthenticationRequest request);
    AuthenticationResponse registerOrLogin(String email);

}
