package com.sma.core.utils;

import com.sma.core.enums.Role;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public class JwtTokenProvider {

    public static Integer getCurrentUserId() {
        Jwt jwt = getJwt();
        if (jwt == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return getIntClaim(jwt, "userId");
    }

    public static Role getCurrentRole() {
        Jwt jwt = getJwt();
        if (jwt == null) {
            return null;
        }
        String scope = jwt.getClaim("scope");
        if (scope == null || scope.isBlank()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String roleName = scope.replace("ROLE_", "");
        return Role.valueOf(roleName);
    }

    public static Integer getCurrentCandidateId() {
        Jwt jwt = getJwt();
        if (jwt == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return getIntClaim(jwt, "candidateId");
    }

    public static Integer getCurrentRecruiterId() {
        Jwt jwt = getJwt();
        if (jwt == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return getIntClaim(jwt, "recruiterId");
    }

    private static Integer getIntClaim(Jwt jwt, String claimName) {
        Object value = jwt.getClaim(claimName);
        if (value == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    private static Jwt getJwt() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }

        return jwt;
    }

    // public static boolean hasRole(String token, Role role) { }
}
