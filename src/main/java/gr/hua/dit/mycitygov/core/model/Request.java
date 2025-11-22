package gr.hua.dit.mycitygov.core.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "requests",
    indexes = {
        @Index(columnList = "status"),
        @Index(columnList = "submittedAt")
    })
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Citizen citizen;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private RequestType type;

    @Column(nullable = false)
    private String title;

    @Column(length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.SUBMITTED;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant submittedAt;

    public Long getId() { return id; }

    public Citizen getCitizen() { return citizen; }

    public void setCitizen(Citizen citizen) { this.citizen = citizen; }

    public RequestType getType() { return type; }

    public void setType(RequestType type) { this.type = type; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public RequestStatus getStatus() { return status; }

    public void setStatus(RequestStatus status) { this.status = status; }

    public Instant getSubmittedAt() { return submittedAt; }

    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
}
