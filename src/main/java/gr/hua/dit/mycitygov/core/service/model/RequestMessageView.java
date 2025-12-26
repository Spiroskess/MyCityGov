package gr.hua.dit.mycitygov.core.service.model;

import gr.hua.dit.mycitygov.core.model.RequestMessageType;

import java.time.Instant;

public record RequestMessageView(
    Long id,
    Instant createdAt,
    String createdBy,
    RequestMessageType type,
    String message
) {}
