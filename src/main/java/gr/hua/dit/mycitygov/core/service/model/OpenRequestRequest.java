package gr.hua.dit.mycitygov.core.service.model;

import gr.hua.dit.mycitygov.core.model.RequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OpenRequestRequest(
    @NotNull RequestType type,
    @NotNull @NotBlank @Size(max = 255) String subject,
    @NotNull @NotBlank @Size(max = 2000) String description
) {}
