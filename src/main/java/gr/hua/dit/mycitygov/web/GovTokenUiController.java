package gr.hua.dit.mycitygov.web;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.govauth.GovAuthClient;
import gr.hua.dit.mycitygov.govauth.GovAuthException;
import gr.hua.dit.mycitygov.govauth.GovLoginService;
import gr.hua.dit.mycitygov.govauth.dto.CitizenIdentityDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class GovTokenUiController {

    private static final String SESSION_GOV_ID = "GOV_IDENTITY";
    private static final String SESSION_GOV_TOKEN = "GOV_TOKEN";
    private static final String SESSION_GOV_CONFIRMED = "GOV_CONFIRMED";

    private final GovAuthClient govAuthClient;
    private final GovLoginService govLoginService;
    private final AuthenticationManager authenticationManager;

    private final SecurityContextRepository securityContextRepository;

    public GovTokenUiController(GovAuthClient govAuthClient,
                                GovLoginService govLoginService,
                                AuthenticationManager authenticationManager,
                                SecurityContextRepository securityContextRepository) {
        this.govAuthClient = govAuthClient;
        this.govLoginService = govLoginService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }


    // 1) Σελίδα εισαγωγής token
    @GetMapping("/gov-token-login")
    public String tokenPage() {
        return "gov-token-login";
    }

    // 2) Υποβολή token -> validate -> αποθήκευση identity+token στο session -> confirm
    @PostMapping("/gov-token-login")
    public String submitToken(@RequestParam("govToken") String govToken,
                              HttpSession session) {
        try {
            CitizenIdentityDto dto = govAuthClient.validateToken(govToken);

            session.setAttribute(SESSION_GOV_ID, dto);
            session.setAttribute(SESSION_GOV_TOKEN, govToken);
            session.removeAttribute(SESSION_GOV_CONFIRMED);

            return "redirect:/gov-token-login/confirm";
        } catch (GovAuthException ex) {
            return "redirect:/gov-token-login?error=1";
        }
    }

    // 3) Σελίδα confirm
    @GetMapping("/gov-token-login/confirm")
    public String confirmPage(HttpSession session) {
        if (session.getAttribute(SESSION_GOV_ID) == null) {
            return "redirect:/gov-token-login?error=1";
        }
        return "gov-confirm";
    }

    // 4) Confirm -> αν σωστό -> πάει στο profile
    @PostMapping("/gov-token-login/confirm")
    public String confirm(@RequestParam("amkaLast4") String amkaLast4,
                          HttpSession session) {

        Object obj = session.getAttribute(SESSION_GOV_ID);
        String token = (String) session.getAttribute(SESSION_GOV_TOKEN);

        if (!(obj instanceof CitizenIdentityDto dto) || token == null || token.isBlank()) {
            return "redirect:/gov-token-login?error=1";
        }

        String amka = dto.amka();
        String last4 = (amka != null && amka.length() >= 4) ? amka.substring(amka.length() - 4) : "";

        if (!last4.equals(amkaLast4.trim())) {
            return "redirect:/gov-token-login/confirm?error=1";
        }

        session.setAttribute(SESSION_GOV_CONFIRMED, Boolean.TRUE);
        return "redirect:/gov-token-login/profile";
    }

    // 5) Προβολή στοιχείων (μόνο αν έχει γίνει confirm)
    @GetMapping("/gov-token-login/profile")
    public String profile(HttpSession session, Model model) {

        Object obj = session.getAttribute(SESSION_GOV_ID);
        Boolean confirmed = (Boolean) session.getAttribute(SESSION_GOV_CONFIRMED);

        if (!(obj instanceof CitizenIdentityDto dto) || confirmed == null || !confirmed) {
            return "redirect:/gov-token-login?error=1";
        }

        model.addAttribute("c", dto);
        return "gov-profile";
    }

    // 6) Πατάει "Είσοδος στην εφαρμογή" -> κάνει πραγματικό login + redirect
    @PostMapping("/gov-token-login/enter")
    public String enterApp(HttpServletRequest request, HttpServletResponse response) {

        HttpSession session = request.getSession(false);
        if (session == null) return "redirect:/gov-token-login?error=1";

        Object obj = session.getAttribute(SESSION_GOV_ID);
        String token = (String) session.getAttribute(SESSION_GOV_TOKEN);
        Boolean confirmed = (Boolean) session.getAttribute(SESSION_GOV_CONFIRMED);

        if (!(obj instanceof CitizenIdentityDto) || token == null || token.isBlank() || confirmed == null || !confirmed) {
            return "redirect:/gov-token-login?error=1";
        }

        // 1) Upsert citizen στη βάση
        Person person = govLoginService.authenticateAndUpsertCitizen(token);

        // 2) Authenticate στο Spring Security
        String username = person.getEmailAddress();
        String rawPassword = "Gov-" + person.getAmka();

        var authRequest = new UsernamePasswordAuthenticationToken(username, rawPassword);
        var authentication = authenticationManager.authenticate(authRequest);
        request.changeSessionId();


        // 3) Φτιάχνουμε SecurityContext και ΤΟ ΣΩΖΟΥΜΕ ΣΤΟ SESSION
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // αυτό είναι που “κλειδώνει” το login και δεν σε πετάει έξω στο redirect
        securityContextRepository.saveContext(context, request, response);

        // cleanup του gov flow
        session.removeAttribute(SESSION_GOV_ID);
        session.removeAttribute(SESSION_GOV_TOKEN);
        session.removeAttribute(SESSION_GOV_CONFIRMED);

        // 4) Redirect στη μέσα σελίδα
        return "redirect:/citizen/requests";
    }
}
