package gr.hua.dit.mycitygov.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * DB-driven τύπος αιτήματος.
 * Admin μπορεί να προσθέτει/ενεργοποιεί/απενεργοποιεί και να αλλάζει SLA.
 */
@Entity
@Table(
    name = "request_types",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_request_type_code", columnNames = {"code"})
    }
)
public class RequestTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull @NotBlank
    @Size(max = 64)
    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @NotNull @NotBlank
    @Size(max = 160)
    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @NotNull
    @Min(1)
    @Column(name = "sla_days", nullable = false)
    private Integer slaDays;

    @NotNull
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = Boolean.TRUE;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getSlaDays() { return slaDays; }
    public void setSlaDays(Integer slaDays) { this.slaDays = slaDays; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
