package gr.hua.dit.mycitygov.web;

import gr.hua.dit.mycitygov.config.MockGovProperties;
import gr.hua.dit.mycitygov.core.service.GovUiLoginService;
import gr.hua.dit.mycitygov.web.security.SessionLoginService;
import gr.hua.dit.mycitygov.web.ui.GovTokenLoginForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class GovUiLoginController {

    private final GovUiLoginService govUiLoginService;
    private final SessionLoginService sessionLoginService;
    private final MockGovProperties mockGovProperties;

    public GovUiLoginController(GovUiLoginService govUiLoginService,
                                SessionLoginService sessionLoginService,
                                MockGovProperties mockGovProperties) {
        this.govUiLoginService = govUiLoginService;
        this.sessionLoginService = sessionLoginService;
        this.mockGovProperties = mockGovProperties;
    }

    @GetMapping("/gov-token-login")
    public String legacyRedirect() {
        // Legacy endpoint: κρατήθηκε για παλιά links -> στέλνει στο νέο /gov/login
        return "redirect:/gov/login";
    }

    @GetMapping("/gov/login")
    public String page(@RequestParam(value = "token", required = false) String token,
                       HttpServletRequest request,
                       HttpServletResponse response,
                       Model model) {

        // Auto-login: αν ο MockGov επέστρεψε με ?token=..., κάνε validate + login στο session
        if (token != null && !token.isBlank()) {
            try {
                var person = govUiLoginService.loginOrRegisterCitizenByToken(token.trim());
                sessionLoginService.loginByEmail(person.getEmailAddress(), request, response);
                return "redirect:/citizen/dashboard";
            } catch (Exception ex) {
                // Fallback σε manual login με μήνυμα λάθους
                model.addAttribute("errorMessage", "Το token δεν είναι έγκυρο ή έχει λήξει. Δοκίμασε ξανά.");
            }
        }

        // Health/status ping στον provider (GET προς external service)
        try {
            govUiLoginService.pingProvider();
            model.addAttribute("providerOk", true);
        } catch (Exception ex) {
            model.addAttribute("providerOk", false);
        }

        // Form model για manual εισαγωγή token
        model.addAttribute("govTokenLoginForm", new GovTokenLoginForm(token));

        // Link προς το MockGov UI με returnTo=την τρέχουσα σελίδα (/gov/login)
        String returnTo = absoluteUrl(request, "/gov/login");
        model.addAttribute("mockGovUiUrl", buildMockGovUiLoginUrl(returnTo));

        return "gov/login";
    }

    @PostMapping("/gov/login")
    public String submit(@Valid @ModelAttribute("govTokenLoginForm") GovTokenLoginForm form,
                         BindingResult bindingResult,
                         Model model,
                         HttpServletRequest request,
                         HttpServletResponse response) {

        // Επαναφόρτωση provider status + link για να υπάρχει πάντα στο template
        try {
            govUiLoginService.pingProvider();
            model.addAttribute("providerOk", true);
        } catch (Exception ex) {
            model.addAttribute("providerOk", false);
        }
        model.addAttribute("mockGovUiUrl", buildMockGovUiLoginUrl(absoluteUrl(request, "/gov/login")));

        if (bindingResult.hasErrors()) {
            return "gov/login";
        }

        // Manual login: validate token -> (find or create citizen) -> login στο session
        try {
            var person = govUiLoginService.loginOrRegisterCitizenByToken(form.getUserToken());
            sessionLoginService.loginByEmail(person.getEmailAddress(), request, response);
            return "redirect:/citizen/requests";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Αποτυχία ταυτοποίησης. Έλεγξε το token και ξαναδοκίμασε.");
            return "gov/login";
        }
    }

    // Χτίζει το external login URL του MockGov με returnTo callback πίσω στο MyCityGov
    private String buildMockGovUiLoginUrl(String returnToAbsolute) {
        String encoded = URLEncoder.encode(returnToAbsolute, StandardCharsets.UTF_8);
        return mockGovProperties.publicBaseUrl() + "/external-auth/ui/login?returnTo=" + encoded;
    }

    // Δημιουργεί absolute URL  για να δουλεύει σωστά το returnTo σε local/docker
    private String absoluteUrl(HttpServletRequest request, String path) {
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        String base = scheme + "://" + host + ":" + port;
        if (path.startsWith("/")) return base + path;
        return base + "/" + path;
    }
}
