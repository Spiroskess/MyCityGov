package gr.hua.dit.mycitygov.web.ui;

import gr.hua.dit.mycitygov.core.security.CurrentUserProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Homepage: αν δεν είναι logged-in -> δείχνει homepage.
 * Αν είναι logged-in -> redirect στο /dashboard (και από εκεί γίνεται role-based redirect).
 */
@Controller
public class HomepageController {

    private final CurrentUserProvider currentUserProvider;

    public HomepageController(final CurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/")
    public String showHomepage(final Authentication authentication) {
        if (!AuthUtils.isAuthenticated(authentication)) {
            return "homepage";
        }

        // Αν για κάποιο λόγο δεν βρεθεί current user, μην σκάει -> γύρνα homepage
        if (currentUserProvider.getCurrentUser().isEmpty()) {
            return "homepage";
        }

        // Ένας δρόμος για όλους: /dashboard (εκεί γίνεται redirect ανά ρόλο)
        return "redirect:/dashboard";
    }
}
