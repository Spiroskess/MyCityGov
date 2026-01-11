package gr.hua.dit.mycitygov.core.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "request_message")
public class RequestMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Μήνυμα/σχόλιο που ανήκει σε συγκεκριμένο αίτημα (Many-to-One)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    // Χρόνος δημιουργίας μηνύματος
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    // Κείμενο μηνύματος (π.χ. παρατηρήσεις υπαλλήλου)
    @Column(nullable = false, length = 2048)
    private String message;

    // Τύπος μηνύματος (π.χ. GENERAL / STATUS_CHANGE / SYSTEM)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestMessageType type = RequestMessageType.GENERAL;

    // Αν θα το βλέπει ο πολίτης στο UI
    @Column(nullable = false)
    private boolean visibleToCitizen = true;

    // Ποιος το έγραψε (π.χ. Employee/Admin display name)
    @Column(length = 200)
    private String createdBy;

    public Long getId() { return id; }

    public Request getRequest() { return request; }
    public void setRequest(Request request) { this.request = request; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public RequestMessageType getType() { return type; }
    public void setType(RequestMessageType type) { this.type = type; }

    public boolean isVisibleToCitizen() { return visibleToCitizen; }
    public void setVisibleToCitizen(boolean visibleToCitizen) { this.visibleToCitizen = visibleToCitizen; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
