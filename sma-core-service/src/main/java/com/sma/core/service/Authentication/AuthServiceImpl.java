package com.sma.core.service.Authentication;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.sma.core.service.AuthService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthServiceImpl implements AuthService {

//    @Value("${google.oauth.client-id}")
//    String clientId;

    public GoogleIdToken.Payload verifyGoogleIdToken(String idTokenString) {
//        try {
//            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
//                    .setAudience(Collections.singletonList(clientId))
//                    .build();
//
//            GoogleIdToken idToken = verifier.verify(idTokenString);
//            if (idToken != null) {
//                return idToken.getPayload();
//            } else {
//                log.warn("Invalid Google ID token: {}", idTokenString);
//
//            }
//        } catch (Exception e) {
//            log.warn("Google ID token verification failed", e);
//            throw new RuntimeException(e);
//        }
        return new GoogleIdToken.Payload();
    }



}
