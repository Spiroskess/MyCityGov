package gr.hua.dit.mycitygov.web.rest.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class BookAppointmentRequest {

    @NotNull
    @Schema(example = "KEP")
    public MunicipalService service;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(example = "2026-01-02")
    public LocalDate date;

    @NotNull
    @JsonFormat(pattern = "HH:mm")
    @Schema(example = "10:30")
    public LocalTime time;
}
