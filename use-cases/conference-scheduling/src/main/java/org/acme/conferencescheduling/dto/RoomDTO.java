package org.acme.conferencescheduling.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A room in which talks can be scheduled.")
public record RoomDTO(
        @Schema(description = "Unique identifier of the room.") String id,
        @Schema(description = "Display name of the room.") String name,
        @Schema(description = "Seating capacity of the room.") int capacity,
        @Schema(description = "Names of the talk types compatible with this room.") List<String> talkTypeNames,
        @Schema(description = "IDs of the timeslots during which this room is unavailable.") List<String> unavailableTimeslotIds,
        @Schema(description = "Tags describing this room.") List<String> tags) {

    public RoomDTO {
        name = name == null ? "" : name;
        talkTypeNames = talkTypeNames == null ? List.of() : List.copyOf(talkTypeNames);
        unavailableTimeslotIds = unavailableTimeslotIds == null ? List.of() : List.copyOf(unavailableTimeslotIds);
        tags = tags == null ? List.of() : List.copyOf(tags);
    }

    public RoomDTO withId(String id) {
        return new RoomDTO(id, name, capacity, talkTypeNames, unavailableTimeslotIds, tags);
    }

    public RoomDTO withName(String name) {
        return new RoomDTO(id, name, capacity, talkTypeNames, unavailableTimeslotIds, tags);
    }

    public RoomDTO withCapacity(int capacity) {
        return new RoomDTO(id, name, capacity, talkTypeNames, unavailableTimeslotIds, tags);
    }

    public RoomDTO withTalkTypeNames(List<String> talkTypeNames) {
        return new RoomDTO(id, name, capacity, talkTypeNames, unavailableTimeslotIds, tags);
    }

    public RoomDTO withUnavailableTimeslotIds(List<String> unavailableTimeslotIds) {
        return new RoomDTO(id, name, capacity, talkTypeNames, unavailableTimeslotIds, tags);
    }

    public RoomDTO withTags(List<String> tags) {
        return new RoomDTO(id, name, capacity, talkTypeNames, unavailableTimeslotIds, tags);
    }
}
