package gr.hua.dit.mockgov.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record IssueTokenRequest(
    @NotBlank
    @Pattern(regexp = "\\d{9}", message = "ΑΦΜ πρέπει να έχει 9 ψηφία")
    String afm,

    @NotBlank
    @Pattern(regexp = "\\d{11}", message = "ΑΜΚΑ πρέπει να έχει 11 ψηφία")
    String amka,

    @NotBlank
    String lastName
) {}
