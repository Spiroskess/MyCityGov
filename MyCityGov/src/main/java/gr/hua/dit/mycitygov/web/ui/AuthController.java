package gr.hua.dit.mycitygov.web.ui;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller-redirect για /login: δεν έχει ξεχωριστό login view, όλα γίνονται από την homepage.
 */
@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginRedirect(final Authentication authentication,
                                final HttpServletRequest request) {

        // Αν είναι ήδη logged-in, απλά επέστρεψε στο home
        if (AuthUtils.isAuthenticated(authentication)) {
            return "redirect:/";
        }

        // Περνάει error/logout/registered flags στο "/" για να εμφανιστούν alerts στο homepage
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
