package com.sma.core.utils;

import com.sma.core.enums.Role;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public class JwtTokenProvider {

    public static Integer getCurrentUserId() {
        return getIntClaim(getJwt(), "userId");
    }

    public static Role getCurrentRole() {
        Jwt jwt = getJwt();
        String scope = jwt.getClaim("scope");
        if (scope == null || scope.isBlank()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String roleName = scope.replace("ROLE_", "");
        return Role.valueOf(roleName);
    }

    public static Integer getCurrentActorId() {
        Jwt jwt = getJwt();
        String scope = jwt.getClaim("scope");
        if (scope == null || scope.isBlank()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String roleName = scope.replace("ROLE_", "");
        Role role = Role.valueOf(roleName);
        if (role.equals(Role.CANDIDATE))
            return getIntClaim(jwt, "candidateId");
        else if (role.equals(Role.RECRUITER))
            return getIntClaim(jwt, "recruiterId");
        return null;
    }

    public static Integer getCurrentCandidateId() {
        return getIntClaim(getJwt(), "candidateId");
    }

    public static Integer getCurrentRecruiterId() {
        return getIntClaim(getJwt(), "recruiterId");
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
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return jwt;
    }

    // public static boolean hasRole(String token, Role role) { }
}
