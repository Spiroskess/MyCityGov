package gr.hua.dit.mycitygov.core.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments",
    indexes = {
        @Index(columnList = "startAt")
    })
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Citizen citizen;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private RequestType type;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private Integer durationMinutes = 30;

    public Long getId() { return id; }

    public Citizen getCitizen() { return citizen; }

    public void setCitizen(Citizen citizen) { this.citizen = citizen; }

    public RequestType getType() { return type; }

    public void setType(RequestType type) { this.type = type; }

    public LocalDateTime getStartAt() { return startAt; }

    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }

    public Integer getDurationMinutes() { return durationMinutes; }

    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
}
