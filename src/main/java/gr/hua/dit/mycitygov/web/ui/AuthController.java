package gr.hua.dit.mycitygov.web.ui;


import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Απλός controller για login/logout views.
 */
@Controller
public class AuthController {

    @GetMapping("/login")
    public String login(
        final Authentication authentication,
        final HttpServletRequest request,
        final Model model
    ) {
        if (AuthUtils.isAuthenticated(authentication)) {
            return "redirect:/profile";
        }

        if (request.getParameter("error") != null) {
            model.addAttribute("error", "Invalid email or password.");
        }
        if (request.getParameter("logout") != null) {
            model.addAttribute("message", "You have been logged out.");
        }
        return "login";
    }

    @GetMapping("/logout")
    public String logout(final Authentication authentication) {
        if (AuthUtils.isAnonymous(authentication)) {
            return "redirect:/login";
        }
        return "logout";
    }
}
