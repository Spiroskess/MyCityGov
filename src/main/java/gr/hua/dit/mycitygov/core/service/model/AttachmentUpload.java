package gr.hua.dit.mycitygov.core.service.model;

import java.io.InputStream;

public record AttachmentUpload(
    String originalFilename,
    String contentType,
    long sizeBytes,
    InputStream inputStream
) {}
