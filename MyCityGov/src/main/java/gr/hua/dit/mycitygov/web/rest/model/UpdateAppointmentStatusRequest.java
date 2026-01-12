package gr.hua.dit.mycitygov.web.rest.model;

import gr.hua.dit.mycitygov.core.service.model.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class UpdateAppointmentStatusRequest {

    @NotNull
    @Schema(example = "CONFIRMED", description = "REQUESTED, CONFIRMED, CANCELLED, COMPLETED")
    public AppointmentStatus status;
}
