package gr.hua.dit.mockgov.api;

import jakarta.validation.constraints.NotBlank;

public record ValidateTokenRequest(
    @NotBlank String userToken
) {}
