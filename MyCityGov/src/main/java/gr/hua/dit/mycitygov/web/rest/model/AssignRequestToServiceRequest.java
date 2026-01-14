package gr.hua.dit.mycitygov.web.rest.model;

import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class AssignRequestToServiceRequest {
    @NotNull
    @Schema(example = "KEP")
    public MunicipalService service;
}
