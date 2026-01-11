package gr.hua.dit.mycitygov.core.service.model;

import java.io.InputStream;
// DTO για download συνημμένου
public record AttachmentDownload(
    String originalFilename,
    String contentType,
    long sizeBytes,
    InputStream inputStream
) {}
