package gr.hua.dit.mycitygov.web;

import gr.hua.dit.mycitygov.govauth.GovAuthClient;
import gr.hua.dit.mycitygov.govauth.GovAuthException;
import gr.hua.dit.mycitygov.mockgov.dto.CitizenIdentityDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class GovLoginController {

    private static final String SESSION_GOV_ID = "GOV_IDENTITY";

    private final GovAuthClient govAuthClient;

    public GovLoginController(GovAuthClient govAuthClient) {
        this.govAuthClient = govAuthClient;
    }

    // (1) Σελίδα εισαγωγής token
    @GetMapping("/gov-login")
    public String govTokenPage() {
        return "gov-token-login"; // θα το φτιάξουμε
    }

    // (2) Υποβολή token: validate → αποθήκευση identity στο session → redirect confirm
    @PostMapping("/gov-login")
    public String submitToken(@RequestParam("govToken") String govToken,
                              HttpSession session) {
        try {
            CitizenIdentityDto dto = govAuthClient.validateToken(govToken);

            // Κρατάμε προσωρινά την ταυτότητα στο session
            session.setAttribute(SESSION_GOV_ID, dto);

            return "redirect:/gov-login/confirm";
        } catch (GovAuthException ex) {
            return "redirect:/gov-login?error=1";
        }
    }

    // (3) Σελίδα επιβεβαίωσης (π.χ. last4 AMKA)
    @GetMapping("/gov-login/confirm")
    public String confirmPage() {
        return "gov-confirm";
    }

    // (4) Επιβεβαίωση: αν ταιριάξει → δείχνουμε στοιχεία
    @PostMapping("/gov-login/confirm")
    public String confirm(@RequestParam("amkaLast4") String amkaLast4,
                          HttpSession session,
                          Model model) {
        Object obj = session.getAttribute(SESSION_GOV_ID);
        if (!(obj instanceof CitizenIdentityDto dto)) {
            return "redirect:/gov-login?error=1";
        }

        String amka = dto.amka();
        String last4 = (amka != null && amka.length() >= 4) ? amka.substring(amka.length() - 4) : "";

        if (!last4.equals(amkaLast4.trim())) {
            return "redirect:/gov-login/confirm?error=1";
        }

        // Επιβεβαιώθηκε → περνάμε τα στοιχεία στη σελίδα
        model.addAttribute("c", dto);

        // προαιρετικά καθαρίζουμε session για να μην μείνει
        session.removeAttribute(SESSION_GOV_ID);

        return "gov-profile";
    }
}
