package gr.hua.dit.mycitygov.web.ui;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Προφίλ χρήστη – τα δεδομένα τα παίρνει από το "me" (CurrentUserControllerAdvice).
 */
@Controller
public class ProfileController {

    @GetMapping("/profile")
    public String showProfile() {
        return "profile";
    }
}
