package gr.hua.dit.mycitygov.core.service.model;

import java.time.Instant;

public record AttachmentView(
    Long id,
    String originalFilename,
    String contentType,
    long sizeBytes,
    Instant uploadedAt
) {}
