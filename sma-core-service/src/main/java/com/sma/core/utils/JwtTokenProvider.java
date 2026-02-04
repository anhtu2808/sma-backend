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
        Integer userId = jwt.getClaim("userId");
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return userId;
    }

    public static Role getCurrentRole() {
        Jwt jwt = getJwt();
        String scope = jwt.getClaim("scope");
        if (scope == null || scope.isBlank()) {
            return null;
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
        Integer actorId = null;
        if (role.equals(Role.CANDIDATE))
            actorId = jwt.getClaim("candidateId");
        else if (role.equals(Role.RECRUITER))
            actorId = jwt.getClaim("recruiterId");
        return actorId;
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

//    public static boolean hasRole(String token, Role role) { }
}
