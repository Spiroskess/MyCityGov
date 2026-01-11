package gr.hua.dit.mycitygov.web;

import gr.hua.dit.mycitygov.core.security.ApplicationUserDetails;
import org.springframework.security.core.Authentication;

/**
 * Helper: παίρνει το personId του συνδεδεμένου χρήστη από το Spring Security principal (ApplicationUserDetails).
 */
public final class CurrentUserIds {

    private CurrentUserIds() {}

    public static Long currentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Unauthenticated user");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof ApplicationUserDetails aud) {
            return aud.personId();
        }

        throw new IllegalStateException(
            "Unsupported principal type: " + principal.getClass()
        );
    }
}
