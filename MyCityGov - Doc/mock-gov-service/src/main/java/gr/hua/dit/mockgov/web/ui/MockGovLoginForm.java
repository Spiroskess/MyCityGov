package gr.hua.dit.mockgov.web.ui;

import jakarta.validation.constraints.NotBlank;

/**
 * Απλό UI form του MockGov (ο "πάροχος") που εκδίδει userToken.
 * Αυτό ΔΕΝ είναι το secured external API του client.
 */
public class MockGovLoginForm {

    @NotBlank(message = "ΑΦΜ: υποχρεωτικό")
    private String afm;

    @NotBlank(message = "ΑΜΚΑ: υποχρεωτικό")
    private String amka;

    @NotBlank(message = "Επώνυμο: υποχρεωτικό")
    private String lastName;

    public String getAfm() { return afm; }
    public void setAfm(String afm) { this.afm = afm; }

    public String getAmka() { return amka; }
    public void setAmka(String amka) { this.amka = amka; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}
