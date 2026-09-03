package org.acme.maintenancescheduling.dto.input;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A maintenance crew that can be assigned to jobs.")
public record CrewInputDTO(
        @Schema(description = "Unique identifier of the crew.", required = true, minLength = 1) String id,
        @Schema(description = "Display name of the crew.", required = true, minLength = 1) String name) {
}
