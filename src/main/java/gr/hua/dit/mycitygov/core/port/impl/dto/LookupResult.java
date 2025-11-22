package gr.hua.dit.mycitygov.core.port.impl.dto;

import gr.hua.dit.mycitygov.core.model.PersonType;

/**
 * LookupResult DTO.
 */
public record LookupResult(
    String raw,
    boolean exists,
    String huaId,
    PersonType type
) {}
