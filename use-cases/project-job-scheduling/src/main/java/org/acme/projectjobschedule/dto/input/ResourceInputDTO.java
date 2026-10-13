package org.acme.projectjobschedule.dto.input;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Something a job consumes while it executes, e.g. a machine or a crew.")
public record ResourceInputDTO(
        @Schema(description = "Unique identifier of the resource.", required = true, minLength = 1) String id,
        @Schema(description = "Units of this resource available: per day when it is renewable, over the whole "
                + "schedule when it is not.", required = true, minimum = "1") Integer capacity,
        @Schema(description = "ID of the project this resource belongs to, or null if every project draws from it.",
                minLength = 1) String projectId,
        @Schema(description = "Whether the resource is freed again once a job releases it, making its capacity a "
                + "limit per day instead of a limit over the whole schedule.", required = true) Boolean renewable) {
}
