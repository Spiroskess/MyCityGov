package gr.hua.dit.mycitygov.web.ui;

import gr.hua.dit.mycitygov.core.security.CurrentUser;
import gr.hua.dit.mycitygov.core.security.CurrentUserProvider;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Βάζει το αντικείμενο "me" σε κάθε model, όπως στο OfficeHours.
 */
@ControllerAdvice
public class CurrentUserControllerAdvice {

    private final CurrentUserProvider currentUserProvider;

    public CurrentUserControllerAdvice(final CurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    @ModelAttribute("me")
    public CurrentUser addCurrentUserToModel() {
        return currentUserProvider.getCurrentUser().orElse(null);
    }
}
