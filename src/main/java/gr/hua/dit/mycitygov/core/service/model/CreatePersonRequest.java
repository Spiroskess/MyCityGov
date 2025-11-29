package gr.hua.dit.mycitygov.core.service.model;

import gr.hua.dit.mycitygov.core.model.PersonRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePersonRequest(
    PersonRole role,
    @NotNull @NotBlank @Size(max = 100) @Email String emailAddress,
    @NotNull @NotBlank @Size(max = 100) String firstName,
    @NotNull @NotBlank @Size(max = 100) String lastName,
    @NotNull @NotBlank @Size(max = 18) String mobilePhoneNumber,
    @NotNull @NotBlank @Size(max = 11) String afm,
    @NotNull @NotBlank @Size(max = 11) String amka,
    @NotNull @NotBlank @Size(min = 4, max = 24) String rawPassword
) {}
