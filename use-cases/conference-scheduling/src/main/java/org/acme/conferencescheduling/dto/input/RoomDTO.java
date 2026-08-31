package org.acme.conferencescheduling.dto.input;

import static java.util.Collections.emptyList;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A room in which talks can be scheduled.")
public record RoomDTO(
        @Schema(description = "Unique identifier of the room.", required = true, minLength = 1) String id,
        @Schema(description = "Display name of the room.", required = true, minLength = 1) String name,
        @Schema(description = "Seating capacity of the room.", required = true, minimum = "1") Integer capacity,
        @Schema(description = "Names of the talk types compatible with this room.", required = true,
                minItems = 1) List<String> talkTypeNames,
        @Schema(description = "IDs of the timeslots during which this room is unavailable.") List<String> unavailableTimeslotIds,
        @Schema(description = "Tags describing this room.") List<String> tags) {

    public RoomDTO {
        unavailableTimeslotIds = unavailableTimeslotIds != null ? unavailableTimeslotIds : emptyList();
        tags = tags != null ? tags : emptyList();
    }
}
