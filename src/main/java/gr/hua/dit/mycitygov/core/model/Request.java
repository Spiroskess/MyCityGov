package gr.hua.dit.mycitygov.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "requests")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 20)
    @Column(name = "protocol_number", nullable = false, length = 20, unique = true)
    private String protocolNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private RequestType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private RequestStatus status = RequestStatus.SUBMITTED;

    // ο πολίτης που έκανε το αίτημα
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_request_citizen"))
    private Person citizen;

    // ο υπάλληλος στον οποίο ανατέθηκε (μπορεί να είναι null αρχικά)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id",
        foreignKey = @ForeignKey(name = "fk_request_employee"))
    private Person assignedEmployee;

    @NotNull
    @NotBlank
    @Size(max = 255)
    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @NotNull
    @NotBlank
    @Size(max = 2000)
    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Column(name = "sla_due_date")
    private LocalDate slaDueDate;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProtocolNumber() {
        return protocolNumber;
    }

    public void setProtocolNumber(String protocolNumber) {
        this.protocolNumber = protocolNumber;
    }

    public RequestType getType() {
        return type;
    }

    public void setType(RequestType type) {
        this.type = type;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public Person getCitizen() {
        return citizen;
    }

    public void setCitizen(Person citizen) {
        this.citizen = citizen;
    }

    public Person getAssignedEmployee() {
        return assignedEmployee;
    }

    public void setAssignedEmployee(Person assignedEmployee) {
        this.assignedEmployee = assignedEmployee;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getSlaDueDate() {
        return slaDueDate;
    }

    public void setSlaDueDate(LocalDate slaDueDate) {
        this.slaDueDate = slaDueDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
