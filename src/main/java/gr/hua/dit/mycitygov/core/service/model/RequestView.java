package gr.hua.dit.mycitygov.core.service.model;

import gr.hua.dit.mycitygov.core.model.RequestStatus;
import gr.hua.dit.mycitygov.core.model.RequestType;

import java.time.Instant;

public record RequestView(
    Long id,
    String protocolNumber,
    RequestType type,
    RequestStatus status,
    String subject,
    String citizenFullName,
    String assignedEmployeeFullName,
    Instant createdAt
) {}
