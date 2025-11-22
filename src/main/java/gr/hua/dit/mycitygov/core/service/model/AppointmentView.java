package gr.hua.dit.mycitygov.core.service.model;

import java.time.LocalDateTime;

public class AppointmentView {

    private Long id;
    private String typeLabel;
    private LocalDateTime startAt;
    private Integer durationMinutes;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getTypeLabel() { return typeLabel; }

    public void setTypeLabel(String typeLabel) { this.typeLabel = typeLabel; }

    public LocalDateTime getStartAt() { return startAt; }

    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }

    public Integer getDurationMinutes() { return durationMinutes; }

    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
}
