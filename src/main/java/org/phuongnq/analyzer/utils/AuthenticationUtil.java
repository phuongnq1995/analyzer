package org.phuongnq.analyzer.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthenticationUtil {

    private AuthenticationUtil() {
        // Utility class
    }

    public static String getCurrentUsername() {
        return getAuthentication().getName();
    }

    private static Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException();
        }
        return authentication;
    }
}
