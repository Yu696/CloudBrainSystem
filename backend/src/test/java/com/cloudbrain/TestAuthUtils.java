package com.cloudbrain;

import com.cloudbrain.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

/**
 * Helper to set up authentication context in tests.
 */
public final class TestAuthUtils {

    private TestAuthUtils() {}

    /**
     * Set up a test user in SecurityContext with userId "TEST_USER_001".
     */
    public static void setupAuth() {
        setupAuth("TEST_USER_001");
    }

    /**
     * Set up a test user with the given userId.
     */
    public static void setupAuth(String userId) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername("testuser");
        user.setStatus(1);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                user, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    /**
     * Clear the security context.
     */
    public static void clearAuth() {
        SecurityContextHolder.clearContext();
    }
}
