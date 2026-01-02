package gr.hua.dit.mycitygov.web.ui;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Δεν υπάρχει πια login view.
 * Κρατάμε το /login μόνο για redirect (παλιά links / Spring redirects).
 */
@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginRedirect(final Authentication authentication,
                                final HttpServletRequest request) {

        // Αν είναι ήδη συνδεδεμένος, πήγαινέ τον στο "/" 
        if (AuthUtils.isAuthenticated(authentication)) {
            return "redirect:/";
        }

        // Μεταφέρουμε τα query params στο homepage για να εμφανίζονται τα alerts εκεί
        if (request.getParameter("error") != null) {
            return "redirect:/?error=1";
        }
        if (request.getParameter("logout") != null) {
            return "redirect:/?logout=1";
        }
        if (request.getParameter("registered") != null) {
            return "redirect:/?registered=1";
        }

        return "redirect:/";
    }
}
