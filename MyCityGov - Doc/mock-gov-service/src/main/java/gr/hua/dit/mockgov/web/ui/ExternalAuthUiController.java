package gr.hua.dit.mockgov.web.ui;

import gr.hua.dit.mockgov.repository.CitizenDirectory;
import gr.hua.dit.mockgov.service.CitizenNotFoundException;
import gr.hua.dit.mockgov.service.UserTokenService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/external-auth/ui")
public class ExternalAuthUiController {

    private final CitizenDirectory citizenDirectory;
    private final UserTokenService userTokenService;

    public ExternalAuthUiController(CitizenDirectory citizenDirectory,
                                    UserTokenService userTokenService) {
        this.citizenDirectory = citizenDirectory;
        this.userTokenService = userTokenService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "returnTo", required = false) String returnTo,
                            Model model) {
        model.addAttribute("form", new MockGovLoginForm());
        model.addAttribute("returnTo", returnTo);
        return "ui/login";
    }

    // Αν κάποιος ανοίξει /issue με GET, τον πάμε πίσω στη φόρμα (με σωστό encoding)
    @GetMapping("/issue")
    public String issueGet(@RequestParam(value = "returnTo", required = false) String returnTo) {
        if (returnTo == null || returnTo.isBlank()) return "redirect:/external-auth/ui/login";
        String encoded = URLEncoder.encode(returnTo, StandardCharsets.UTF_8);
        return "redirect:/external-auth/ui/login?returnTo=" + encoded;
    }

    @PostMapping("/issue")
    public String issueToken(@RequestParam(value = "returnTo", required = false) String returnTo,
                             @Valid @ModelAttribute("form") MockGovLoginForm form,
                             BindingResult bindingResult,
                             Model model) {

        model.addAttribute("returnTo", returnTo);

        if (bindingResult.hasErrors()) {
            return "ui/login";
        }

        try {
            var citizen = citizenDirectory
                .findByCredentials(form.getAfm(), form.getAmka(), form.getLastName())
                .orElseThrow(() -> new CitizenNotFoundException("Invalid credentials"));

            String userToken = userTokenService.issue(citizen);

            model.addAttribute("userToken", userToken);

            //  Φτιάχνουμε ΕΔΩ το url επιστροφής με σωστό encoding (όχι στο Thymeleaf)
            String continueUrl = buildContinueUrl(returnTo, userToken);
            model.addAttribute("continueUrl", continueUrl);

            return "ui/token";
        } catch (CitizenNotFoundException ex) {
            model.addAttribute("errorMessage", "Λάθος στοιχεία. Δοκίμασε ξανά.");
            return "ui/login";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Παρουσιάστηκε σφάλμα στον πάροχο MockGov. Δοκίμασε ξανά.");
            return "ui/login";
        }
    }

    private String buildContinueUrl(String returnTo, String userToken) {
        if (returnTo == null || returnTo.isBlank()) return null;
        String rt = returnTo.trim();
        String sep = rt.contains("?") ? "&" : "?";
        String encToken = URLEncoder.encode(userToken, StandardCharsets.UTF_8);
        return rt + sep + "token=" + encToken;
    }
}
