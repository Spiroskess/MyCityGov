package gr.hua.dit.mycitygov.core.service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Τώρα στέλνουμε requestTypeCode (DB-driven).
 */
public record OpenRequestRequest(
    @NotNull @NotBlank @Size(max = 64) String requestTypeCode,
    @NotNull @NotBlank @Size(max = 255) String subject,
    @NotNull @NotBlank @Size(max = 2000) String description
) {}
