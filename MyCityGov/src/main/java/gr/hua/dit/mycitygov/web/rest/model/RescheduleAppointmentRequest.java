package gr.hua.dit.mycitygov.web.rest.model;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class RescheduleAppointmentRequest {

    @NotNull
    public LocalDate date;

    @NotNull
    public LocalTime time;
}
