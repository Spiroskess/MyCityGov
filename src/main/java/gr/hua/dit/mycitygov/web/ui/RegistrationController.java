package gr.hua.dit.mycitygov.web.ui;


import gr.hua.dit.mycitygov.core.model.PersonRole;
import gr.hua.dit.mycitygov.core.service.PersonService;
import gr.hua.dit.mycitygov.core.service.model.CreatePersonRequest;
import gr.hua.dit.mycitygov.core.service.model.CreatePersonResult;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Controller για την εγγραφή χρηστών (Πολιτών κ.λπ.).
 */
@Controller
public class RegistrationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationController.class);

    private final PersonService personService;

    public RegistrationController(final PersonService personService) {
        this.personService = personService;
    }

    /**
     * Εμφάνιση φόρμας εγγραφής.
     */
    @GetMapping("/register")
    public String showRegisterForm(final Authentication authentication, final Model model) {
        // Αν είναι ήδη συνδεδεμένος, στείλ' τον στο profile (ή σε home σελίδα ρόλου).
        if (authentication != null && authentication.isAuthenticated()
            && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/profile";
        }

        // Default ρόλος: Πολίτης
        CreatePersonRequest form = new CreatePersonRequest(
            PersonRole.CITIZEN, // προεπιλογή ρόλου
            "",                 // email
            "",                 // firstName
            "",                 // lastName
            "",                 // mobilePhoneNumber
            "",                 // afm
            "",                 // amka
            ""                  // rawPassword
        );

        model.addAttribute("createPersonRequest", form);
        return "register";
    }

    /**
     * Υποβολή φόρμας εγγραφής.
     */
    @PostMapping("/register")
    public String handleRegister(
        final Authentication authentication,
        @Valid @ModelAttribute("createPersonRequest") final CreatePersonRequest createPersonRequest,
        final BindingResult bindingResult,
        final Model model
    ) {
        // Αν είναι ήδη συνδεδεμένος, μην τον αφήνεις να ξανακάνει register.
        if (authentication != null && authentication.isAuthenticated()
            && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/profile";
        }

        // Bean Validation (NotBlank, Email, Size κλπ)
        if (bindingResult.hasErrors()) {
            LOGGER.warn("Registration validation errors: {}", bindingResult.getAllErrors());
            // Η φόρμα ξαναγυρίζει μαζί με μηνύματα λαθών (th:errors στο template).
            return "register";
        }

        // Κανονική δημιουργία χρήστη μέσω service.
        CreatePersonResult createPersonResult = this.personService.createPerson(createPersonRequest);

        if (createPersonResult.created()) {
            LOGGER.info("New person registered with email={}", createPersonRequest.emailAddress());
            // 1η φάση: μετά την εγγραφή τον στέλνουμε στο login
            // (αν θες auto-login, το φτιάχνουμε σε επόμενο βήμα)
            return "redirect:/login?registered";
        }

        // Αν απέτυχε (π.χ. διπλό email/afm/amka), στείλε μήνυμα λάθους
        model.addAttribute("createPersonRequest", createPersonRequest);
        model.addAttribute("errorMessage", createPersonResult.reason());
        return "register";
    }
}
