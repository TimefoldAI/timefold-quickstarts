package org.acme.tournamentschedule.dto.input;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A team that has to be assigned to matches.")
public record TeamInputDTO(
        @Schema(description = "Unique identifier of the team.", required = true, minLength = 1) String id,
        @Schema(description = "Display name of the team.", required = true, minLength = 1) String name) {
}
