package gr.hua.dit.mycitygov.web.ui;

import gr.hua.dit.mycitygov.config.MockGovProperties;
import gr.hua.dit.mycitygov.core.security.CurrentUserProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class HomepageController {

    private final CurrentUserProvider currentUserProvider;
    private final MockGovProperties mockGovProperties;

    public HomepageController(final CurrentUserProvider currentUserProvider,
                              final MockGovProperties mockGovProperties) {
        this.currentUserProvider = currentUserProvider;
        this.mockGovProperties = mockGovProperties;
    }

    @GetMapping("/")
    public String showHomepage(final Authentication authentication,
                               final HttpServletRequest request,
                               final Model model) {

        // Homepage: αν δεν είναι logged-in, δείχνει link προς MockGov UI
        if (!AuthUtils.isAuthenticated(authentication)) {
            String returnTo = absoluteUrl(request, "/gov/login"); // endpoint που θα δεχτεί το token callback
            String encoded = URLEncoder.encode(returnTo, StandardCharsets.UTF_8);

            String mockGovUiUrl = mockGovProperties.publicBaseUrl()
                + "/external-auth/ui/login?returnTo=" + encoded;

            model.addAttribute("mockGovUiUrl", mockGovUiUrl);
            return "homepage";
        }

        // Αν υπάρχει auth αλλά δεν βρέθηκε Person στο σύστημά μας, μένουμε στο homepage
        if (currentUserProvider.getCurrentUser().isEmpty()) {
            return "homepage";
        }

        return "redirect:/dashboard";
    }

    private String absoluteUrl(HttpServletRequest request, String path) {
        // Helper: φτιάχνει absolute URL (scheme/host/port) για να το δώσουμε ως returnTo στο MockGov
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        String base = scheme + "://" + host + ":" + port;
        return path.startsWith("/") ? base + path : base + "/" + path;
    }
}
