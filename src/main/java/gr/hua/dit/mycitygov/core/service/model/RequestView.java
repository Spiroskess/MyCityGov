package gr.hua.dit.mycitygov.core.service.model;

import java.time.Instant;

public class RequestView {

    private Long id;
    private String typeLabel;
    private String title;
    private String description;
    private String status;
    private Instant submittedAt;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getTypeLabel() { return typeLabel; }

    public void setTypeLabel(String typeLabel) { this.typeLabel = typeLabel; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public Instant getSubmittedAt() { return submittedAt; }

    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
}
