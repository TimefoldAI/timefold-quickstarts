package org.acme.schooltimetabling.dto.input;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A room lessons are taught in. It holds one lesson per timeslot.")
public record RoomInputDTO(
        @Schema(description = "Unique identifier of the room.", required = true, minLength = 1) String id,
        @Schema(description = "Human-readable name of the room.", required = true, minLength = 1) String name) {
}
