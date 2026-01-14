package gr.hua.dit.mycitygov.web.rest.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * REST registration request for a Citizen.
 * UI registration already exists; this DTO enables API/SPA/mobile sign-up.
 */
public class RegisterCitizenRequest {

    @NotBlank
    @Email
    @Size(max = 100)
    public String email;

    @NotBlank
    @Size(max = 100)
    public String firstName;

    @NotBlank
    @Size(max = 100)
    public String lastName;

    @NotBlank
    @Size(max = 18)
    public String mobilePhoneNumber;

    @NotBlank
    public String afm;

    @NotBlank
    public String amka;

    @NotBlank
    public String password;
}
