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

@Controller
public class RegistrationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationController.class);

    private final PersonService personService;

    public RegistrationController(final PersonService personService) {
        if (personService == null) throw new NullPointerException("personService");
        this.personService = personService;
    }

    @GetMapping("/register")
    public String showRegister(final Authentication authentication, final Model model) {
        // UI registration σελίδα: αν είναι ήδη logged-in, redirect στο profile
        if (authentication != null && authentication.isAuthenticated()
            && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/profile";
        }

        // Default φόρμα CreatePersonRequest (DTO) με ρόλο CITIZEN
        CreatePersonRequest form = new CreatePersonRequest(
            PersonRole.CITIZEN,
            "",
            "",
            "",
            "",
            "",
            "",
            ""
        );

        model.addAttribute("createPersonRequest", form);
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(
        final Authentication authentication,
        @Valid @ModelAttribute("createPersonRequest") final CreatePersonRequest createPersonRequest,
        final BindingResult bindingResult,
        final Model model
    ) {
        // Submit registration: validation στο controller + business στο service
        if (authentication != null && authentication.isAuthenticated()
            && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/profile";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("createPersonRequest", createPersonRequest);
            return "register";
        }

        CreatePersonResult createPersonResult = this.personService.createPerson(createPersonRequest, true);

        if (createPersonResult.created()) {
            LOGGER.info("New person registered with email={}", createPersonRequest.emailAddress());
            return "redirect:/?registered=1";
        }

        // Αν απέτυχε (π.χ. phone validation), δείχνει errorMessage στο UI
        model.addAttribute("createPersonRequest", createPersonRequest);
        model.addAttribute("errorMessage", createPersonResult.reason());
        return "register";
    }
}
