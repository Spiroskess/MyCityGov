package gr.hua.dit.mycitygov.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Entity
@Table(name = "request_attachments")
public class RequestAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Συνημμένο που ανήκει σε συγκεκριμένο αίτημα (Many-to-One)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    // Το "κλειδί" του αρχείου στο S3/MinIO (path/object name)
    @NotBlank
    @Column(name = "object_key", nullable = false, length = 600)
    private String objectKey;

    // Το αρχικό όνομα αρχείου που ανέβασε ο χρήστης
    @NotBlank
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    // MIME type (π.χ. application/pdf)
    @Column(name = "content_type", length = 120)
    private String contentType;

    // Μέγεθος αρχείου σε bytes
    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    // Χρόνος upload
    @NotNull
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt = Instant.now();

    public Long getId() { return id; }

    public Request getRequest() { return request; }
    public void setRequest(Request request) { this.request = request; }

    public String getObjectKey() { return objectKey; }
    public void setObjectKey(String objectKey) { this.objectKey = objectKey; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }

    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }
}
