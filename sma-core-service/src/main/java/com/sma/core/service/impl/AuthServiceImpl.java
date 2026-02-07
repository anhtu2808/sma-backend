package com.sma.core.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sma.core.dto.request.auth.LoginRequest;
import com.sma.core.dto.request.auth.LogoutRequest;
import com.sma.core.dto.request.auth.RefreshTokenRequest;
import com.sma.core.dto.request.auth.RegisterRequest;
import com.sma.core.dto.response.auth.AuthenticationResponse;
import com.sma.core.entity.Candidate;
import com.sma.core.entity.User;
import com.sma.core.entity.UserToken;
import com.sma.core.enums.CandidateShowAs;
import com.sma.core.enums.Role;
import com.sma.core.enums.TokenType;
import com.sma.core.enums.UserStatus;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.repository.UserRepository;
import com.sma.core.repository.UserTokenRepository;
import com.sma.core.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${google.client-id}")
    String CLIENT_ID;
    @Value("${jwt.secret}")
    String ACCESS_TOKEN_SECRET;
    @Value("${jwt.expiration}")
    Long TOKEN_EXPIRATION;
    @Value("${jwt.refresh-expiration}")
    Long REFRESH_EXPIRATION;
    @Value("${jwt.refresh-secret}")
    String REFRESH_SECRET;

    final UserRepository userRepository;
    final PasswordEncoder passwordEncoder;
    final UserTokenRepository userTokenRepository;




    @Override
    public AuthenticationResponse registerAsCandidate(RegisterRequest request) {
        log.info("Email: {}, password: {}", request.getEmail(), request.getPassword());
        User oldUser = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (oldUser != null)
            throw new AppException(ErrorCode.USER_EXISTS);
        User user = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .passwordHash(request.getPassword() != null ? passwordEncoder.encode(request.getPassword()) : "")
                .avatar(request.getAvatar() != null ? request.getAvatar() : "DEFAULT AVATAR")
                .status(UserStatus.ACTIVE)
                .role(Role.CANDIDATE)
                .build();
        Candidate candidate = Candidate.builder()
                .user(user)
                .showAs(CandidateShowAs.RESUME)
                .isProfilePublic(Boolean.TRUE)
                .profileCompleteness(0)
                .build();
        user.setCandidate(candidate);
        userRepository.save(user);
        return AuthenticationResponse.builder()
                .accessToken(generateToken(user))
                .refreshToken(generateRefreshToken(user))
                .build();
    }

    @Override
    public AuthenticationResponse registerOrLogin(String idTokenString) {
        var googlePayload = verifyGoogleIdToken(idTokenString);
        var email = googlePayload.getEmail();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null)
            return registerAsCandidate(
                    RegisterRequest.builder()
                            .email(email)
                            .fullName(googlePayload.get("name").toString())
                            .avatar(googlePayload.get("picture").toString())
                            .build());
        return AuthenticationResponse.builder()
                .accessToken(generateToken(user))
                .refreshToken(generateRefreshToken(user))
                .build();
    }

    private GoogleIdToken.Payload verifyGoogleIdToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
                    new GsonFactory())
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                return idToken.getPayload();
            } else {
                log.warn("Invalid Google ID token: {}", idTokenString);
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }
        } catch (Exception e) {
            log.warn("Google ID token verification failed", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean logout(LogoutRequest request) {
        var token = userTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_NOT_EXISTED));
        token.setRevoked(true);
        token.setRevokedAt(LocalDateTime.now());
        userTokenRepository.save(token);
        return true;
    }

    @Override
    public AuthenticationResponse login(LoginRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_EXISTED));
        if (!user.getStatus().equals(UserStatus.ACTIVE))
            throw new AppException(ErrorCode.ACCOUNT_INACTIVE);

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        if (!authenticated) {
            throw new AppException(ErrorCode.PASSWORD_INCORRECT);
        }

        var token = generateToken(user);
        var refreshToken = generateRefreshToken(user);
        return AuthenticationResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        try {
            var signedJWT = verifyRefreshToken(request.getRefreshToken());
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            if (!"refresh_token".equals(claims.getStringClaim("type"))) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }
            var user = userRepository.findByEmail(claims.getSubject())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            var newAccessToken = generateToken(user);
            return AuthenticationResponse.builder()
                    .accessToken(newAccessToken)
                    .build();
        } catch (ParseException | RuntimeException ex) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("smartrecruit.tech")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(TOKEN_EXPIRATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("userId", user.getId())
                .claim("candidateId", user.getCandidate() != null ? user.getCandidate().getId() : null)
                .claim("recruiterId", user.getRecruiter() != null ? user.getRecruiter().getId() : null)
                .claim("scope", buildScope(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jWSObject = new JWSObject(header, payload);
        try {
            jWSObject.sign(new MACSigner(ACCESS_TOKEN_SECRET.getBytes()));
            return jWSObject.serialize();
        } catch (JOSEException e) {
            log.error("An error occurred", e);
            throw new RuntimeException("");
        }
    }

    String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (user.getRole() != null) {
            stringJoiner.add("ROLE_" + user.getRole());
        }
        return stringJoiner.toString();
    }

    String generateRefreshToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        Date expDate = new Date(
                Instant.now().plus(REFRESH_EXPIRATION, ChronoUnit.SECONDS).toEpochMilli());
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("smartrecruit.tech")
                .issueTime(new Date())
                .expirationTime(expDate)
                .jwtID(UUID.randomUUID().toString())
                .claim("type", "refresh_token")
                .claim("userId", user.getId())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jWSObject = new JWSObject(header, payload);
        try {
            jWSObject.sign(new MACSigner(REFRESH_SECRET.getBytes()));
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to sign JWSObject: " + e.getMessage(), e);
        }
        String refreshToken = jWSObject.serialize();
        saveRefreshToken(user, expDate, refreshToken);
        return refreshToken;
    }

    SignedJWT verifyRefreshToken(String token) {
        try {
            JWSVerifier verifier = new MACVerifier(REFRESH_SECRET.getBytes());
            SignedJWT signedJWT = SignedJWT.parse(token);
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            boolean verified = signedJWT.verify(verifier);
            boolean revoked = userTokenRepository.existsUserTokenByTokenAndRevoked(token, true);

            if (!verified) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            if (expiryTime.before(new Date()) || revoked) {
                throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
            }

            return signedJWT;

        } catch (JOSEException | ParseException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    void saveRefreshToken(User user, Date expDate, String refreshToken) {
        LocalDateTime expiresAt = expDate
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        UserToken userToken = UserToken.builder()
                .token(refreshToken)
                .tokenType(TokenType.REFRESH)
                .user(user)
                .expiresAt(expiresAt)
                .build();
        userTokenRepository.save(userToken);
    }
}
