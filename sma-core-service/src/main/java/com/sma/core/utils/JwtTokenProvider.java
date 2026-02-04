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
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String roleName = scope.replace("ROLE_", "");

        return Role.valueOf(roleName);
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
