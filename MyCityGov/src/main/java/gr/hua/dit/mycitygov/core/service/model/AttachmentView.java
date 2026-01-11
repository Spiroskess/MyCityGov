package gr.hua.dit.mycitygov.core.service.model;

import java.time.Instant;
// DTO για εμφάνιση συνημμένου στο UI/REST
public record AttachmentView(
    Long id,
    String originalFilename,
    String contentType,
    long sizeBytes,
    Instant uploadedAt
) {}
