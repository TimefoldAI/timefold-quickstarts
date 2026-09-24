package org.acme.meetingschedule.dto.input;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A room a meeting can be held in.")
public record RoomInputDTO(
        @Schema(description = "Unique identifier of the room.", required = true, minLength = 1) String id,
        @Schema(description = "Display name of the room.", required = true, minLength = 1) String name,
        @Schema(description = "Number of people the room seats.", required = true, minimum = "1") Integer capacity) {
}
