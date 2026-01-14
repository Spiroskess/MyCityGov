package gr.hua.dit.mycitygov.web.rest.model;

import gr.hua.dit.mycitygov.core.service.model.AppointmentStatus;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record AppointmentDto(
    @Schema(example = "1") Long id,
    Long citizenId,
    Long employeeId,
    @Schema(example = "KEP") MunicipalService service,
    LocalDateTime appointmentDateTime,
    @Schema(example = "CONFIRMED") AppointmentStatus status
) {}
