package com.sma.core.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

public interface AuthService {

    GoogleIdToken.Payload verifyGoogleIdToken(String idTokenString);

}
