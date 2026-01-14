package gr.hua.dit.mycitygov.web.rest.model;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminRequestStatsDto(
    @Schema(example = "42") long total,
    @Schema(example = "10") long unassigned,
    @Schema(example = "32") long assigned,
    @Schema(example = "7") long overdue
) {}
