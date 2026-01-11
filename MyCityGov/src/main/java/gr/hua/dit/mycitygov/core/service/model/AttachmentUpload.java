package gr.hua.dit.mycitygov.core.service.model;

import java.io.InputStream;
// DTO για upload συνημμένου
public record AttachmentUpload(
    String originalFilename,
    String contentType,
    long sizeBytes,
    InputStream inputStream
) {}
