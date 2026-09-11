package org.acme.foodpackaging.dto.output;

import java.time.OffsetDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "A job that is either scheduled on a production line, with the times derived from its position "
        + "in that line's job sequence, or not scheduled at all.")
public record JobAssignmentDTO(
        @Schema(description = "Unique identifier of the job.", required = true, minLength = 1) String id,
        @Schema(description = "ID of the line producing this job, or null if unassigned.") String lineId,
        @Schema(description = "Time at which the changeover cleaning before this job starts, or null if unassigned. "
                + "Equal to startProductionDateTime for the first job of a line, which needs no cleaning.") OffsetDateTime startCleaningDateTime,
        @Schema(description = "Time at which the production of this job starts, or null if unassigned.") OffsetDateTime startProductionDateTime,
        @Schema(description = "Time at which the production of this job finishes, or null if unassigned.") OffsetDateTime endDateTime) {
}
