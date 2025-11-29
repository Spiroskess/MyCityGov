package gr.hua.dit.mycitygov.web.ui;

import gr.hua.dit.mycitygov.core.security.CurrentUser;
import gr.hua.dit.mycitygov.core.security.CurrentUserProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Homepage: αν δεν είναι logged-in -> δείχνει homepage.
 * Αν είναι logged-in -> redirect στο dashboard ανά ρόλο.
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

        final CurrentUser me = currentUserProvider.getCurrentUser().orElse(null);
        if (me == null) {
            return "homepage";
        }

        return switch (me.role()) {
            case CITIZEN -> "redirect:/citizen/requests";
            case EMPLOYEE -> "redirect:/employee/requests";
            case ADMIN -> "redirect:/admin/requests";
        };
    }
}
