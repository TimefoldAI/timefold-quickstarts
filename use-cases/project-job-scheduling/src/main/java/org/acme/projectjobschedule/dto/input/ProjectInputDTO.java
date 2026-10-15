package org.acme.projectjobschedule.dto.input;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A project whose jobs have to be scheduled.")
public record ProjectInputDTO(
        @Schema(description = "Unique identifier of the project.", required = true, minLength = 1) String id,
        @Schema(description = "Day number, counted from the start of the schedule, on which this project's jobs may start.",
                required = true, minimum = "0") Integer releaseDate,
        @Schema(description = "Duration in days of the project's critical path, so the shortest possible time it can take.",
                required = true, minimum = "0") Integer criticalPathDuration) {
}
