package org.acme.meetingschedule.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A room in which a meeting can take place.")
public record RoomDTO(
        @Schema(description = "Unique identifier of the room.") String id,
        @Schema(description = "Display name of the room.") String name,
        @Schema(description = "Maximum number of attendees the room can hold.") int capacity) {

    public RoomDTO {
        name = name == null ? "" : name;
    }

    public RoomDTO withId(String id) {
        return new RoomDTO(id, name, capacity);
    }

    public RoomDTO withName(String name) {
        return new RoomDTO(id, name, capacity);
    }

    public RoomDTO withCapacity(int capacity) {
        return new RoomDTO(id, name, capacity);
    }
}
