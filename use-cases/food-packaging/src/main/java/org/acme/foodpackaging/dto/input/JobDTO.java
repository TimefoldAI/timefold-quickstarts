package org.acme.foodpackaging.dto.input;

import java.time.OffsetDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "One packaging run of a single product, to be scheduled on a production line.")
public record JobDTO(
        @Schema(description = "Unique identifier of the job.", required = true, minLength = 1) String id,
        @Schema(description = "Display name of the job.", required = true, minLength = 1) String name,
        @Schema(description = "ID of the product this job packages.", required = true, minLength = 1) String productId,
        @Schema(description = "Time the job takes to produce, in minutes, excluding any changeover cleaning before it.",
                required = true, minimum = "1") Long durationMinutes,
        @Schema(description = "Earliest time at which this job should start, in ISO-8601 date-time format.",
                required = true) OffsetDateTime minStartTime,
        @Schema(description = "Time at which this job would ideally be finished, in ISO-8601 date-time format.",
                required = true) OffsetDateTime idealEndTime,
        @Schema(description = "Latest time at which this job may be finished, in ISO-8601 date-time format.",
                required = true) OffsetDateTime maxEndTime,
        @Schema(description = "Whether this job's position in its line's job sequence is pinned and must not be "
                + "changed by the solver.") Boolean pinned) {
}
