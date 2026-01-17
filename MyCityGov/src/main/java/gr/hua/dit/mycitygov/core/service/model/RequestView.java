package gr.hua.dit.mycitygov.core.service.model;

import gr.hua.dit.mycitygov.core.model.RequestStatus;
import java.time.Instant;
import java.time.LocalDate;

/** View DTO για UI/REST */
public record RequestView(
    Long id,
    String protocolNumber,
    String typeCode,
    String typeTitle,
    RequestStatus status,
    MunicipalService assignedService,
    String subject,
    String description,
    String citizenFullName,
    String assignedEmployeeFullName,
    Instant createdAt,
    LocalDate slaDueDate,
    boolean overdue,
    String statusComment
) {}
