package gr.hua.dit.mycitygov.core.service.model;

import gr.hua.dit.mycitygov.core.model.PersonRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
// DTO για registration χρήστη
public record CreatePersonRequest(
    PersonRole role,
    @NotNull @NotBlank @Size(max = 100) @Email String emailAddress,
    @NotNull @NotBlank @Size(max = 100) String firstName,
    @NotNull @NotBlank @Size(max = 100) String lastName,
    @NotNull @NotBlank @Size(max = 18) String mobilePhoneNumber,

    @NotNull(message = "Το ΑΦΜ είναι υποχρεωτικό.")
    @NotBlank(message = "Το ΑΦΜ είναι υποχρεωτικό.")
    @Size(min = 9, max = 9, message = "Το ΑΦΜ πρέπει να έχει ακριβώς 9 ψηφία.")
    @Pattern(regexp = "\\d+", message = "Το ΑΦΜ πρέπει να περιέχει μόνο ψηφία.")
    String afm,

    @NotNull(message = "Το ΑΜΚΑ είναι υποχρεωτικό.")
    @NotBlank(message = "Το ΑΜΚΑ είναι υποχρεωτικό.")
    @Size(min = 11, max = 11, message = "Το ΑΜΚΑ πρέπει να έχει ακριβώς 11 ψηφία.")
    @Pattern(regexp = "\\d+", message = "Το ΑΜΚΑ πρέπει να περιέχει μόνο ψηφία.")
    String amka,

    @NotNull(message = "Ο κωδικός είναι υποχρεωτικός.")
    @NotBlank(message = "Ο κωδικός είναι υποχρεωτικός.")
    @Size(min = 9, max = 64, message = "Ο κωδικός πρέπει να έχει τουλάχιστον 9 χαρακτήρες.")
    @Pattern(
        regexp = "^(?=.*\\p{L})(?=.*[^\\p{L}\\d]).+$",
        message = "Ο κωδικός πρέπει να περιέχει τουλάχιστον 1 γράμμα και 1 σύμβολο."
    )
    String rawPassword
) {}
