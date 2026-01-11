package gr.hua.dit.mycitygov.web.ui;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * Utility κλάση για έλεγχο authentication σε controllers (authenticated vs anonymous).
 */
final class AuthUtils {

    private AuthUtils() {
        throw new UnsupportedOperationException();
    }

    public static boolean isAuthenticated(final Authentication auth) {
        if (auth == null) return false;
        if (auth instanceof AnonymousAuthenticationToken) return false;
        return auth.isAuthenticated();
    }

    public static boolean isAnonymous(final Authentication auth) {
        if (auth == null) return true;
        if (auth instanceof AnonymousAuthenticationToken) return true;
        return !auth.isAuthenticated();
    }
}
