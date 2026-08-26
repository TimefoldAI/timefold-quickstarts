package org.acme.conferencescheduling.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A room in which talks can be scheduled.")
public record RoomDTO(
        @Schema(description = "Unique identifier of the room.", required = true) String id,
        @Schema(description = "Display name of the room.", required = true) String name,
        @Schema(description = "Seating capacity of the room.", required = true) int capacity,
        @Schema(description = "Names of the talk types compatible with this room.") List<String> talkTypeNames,
        @Schema(description = "IDs of the timeslots during which this room is unavailable.") List<String> unavailableTimeslotIds,
        @Schema(description = "Tags describing this room.") List<String> tags) {
}
