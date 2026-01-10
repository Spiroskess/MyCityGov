package gr.hua.dit.mycitygov.web.ui;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * UI form για σύνδεση πολίτη μέσω MockGov userToken.
 */
public class GovTokenLoginForm {

    @NotBlank(message = "Το gov token είναι υποχρεωτικό")
    @Size(min = 10, max = 500, message = "Το gov token φαίνεται λάθος (μήκος)")
    private String userToken;

    public GovTokenLoginForm() {}

    public GovTokenLoginForm(String userToken) {
        this.userToken = userToken;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }
}
