package gr.hua.dit.mycitygov.core.service.model;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public class CreateAppointmentRequest {

    @NotNull
    private Long requestTypeId;

    @NotNull
    @Future
    private LocalDateTime startAt;

    @NotNull
    @Positive
    private Integer durationMinutes = 30;

    public Long getRequestTypeId() { return requestTypeId; }

    public void setRequestTypeId(Long requestTypeId) { this.requestTypeId = requestTypeId; }

    public LocalDateTime getStartAt() { return startAt; }

    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }

    public Integer getDurationMinutes() { return durationMinutes; }

    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
}
