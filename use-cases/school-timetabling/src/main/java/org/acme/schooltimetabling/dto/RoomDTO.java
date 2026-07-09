package org.acme.schooltimetabling.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A room in which a lesson can be taught.")
public record RoomDTO(
        @Schema(description = "Unique identifier of the room.") String id,
        @Schema(description = "Display name of the room.") String name) {

    public RoomDTO {
        name = name == null ? "" : name;
    }

    public RoomDTO withId(String id) {
        return new RoomDTO(id, name);
    }

    public RoomDTO withName(String name) {
        return new RoomDTO(id, name);
    }
}
