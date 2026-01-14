package gr.hua.dit.mycitygov.web.rest.model;

import gr.hua.dit.mycitygov.core.model.RequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateRequestStatusRestRequest {

    @NotNull
    @Schema(example = "IN_PROGRESS", description = "SUBMITTED, RECEIVED, IN_PROGRESS, WAITING_ADDITIONAL_INFO, COMPLETED, REJECTED")
    public RequestStatus status;

    @Size(max = 2000)
    @Schema(example = "Χρειάζονται επιπλέον δικαιολογητικά...", nullable = true)
    public String comment;
}
