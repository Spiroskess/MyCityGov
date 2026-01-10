package gr.hua.dit.mockgov.web.ui;

import gr.hua.dit.mockgov.repository.CitizenDirectory;
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
public class MockGovUiController {

    private final CitizenDirectory citizenDirectory;
    private final UserTokenService userTokenService;

    public MockGovUiController(CitizenDirectory citizenDirectory, UserTokenService userTokenService) {
        this.citizenDirectory = citizenDirectory;
        this.userTokenService = userTokenService;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "returnTo", required = false) String returnTo,
                        Model model) {

        model.addAttribute("returnTo", returnTo);
        model.addAttribute("form", new MockGovLoginForm());
        return "ui/login";
    }

    @PostMapping("/issue")
    public String issue(@RequestParam(value = "returnTo", required = false) String returnTo,
                        @Valid @ModelAttribute("form") MockGovLoginForm form,
                        BindingResult bindingResult,
                        Model model) {

        model.addAttribute("returnTo", returnTo);

        if (bindingResult.hasErrors()) {
            return "ui/login";
        }

        var citizenOpt = citizenDirectory.findByCredentials(form.getAfm(), form.getAmka(), form.getLastName());
        if (citizenOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Δεν βρέθηκε πολίτης με αυτά τα στοιχεία. Δοκίμασε ξανά.");
            return "ui/login";
        }

        String userToken = userTokenService.issue(citizenOpt.get());
        model.addAttribute("userToken", userToken);
        model.addAttribute("continueUrl", buildContinueUrl(returnTo, userToken));

        return "ui/token";
    }

    private String buildContinueUrl(String returnTo, String token) {
        if (returnTo == null || returnTo.isBlank()) return null;

        String sep = returnTo.contains("?") ? "&" : "?";
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        return returnTo + sep + "token=" + encodedToken;
    }
}
