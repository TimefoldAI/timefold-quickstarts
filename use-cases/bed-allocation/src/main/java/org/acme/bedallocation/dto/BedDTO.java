package org.acme.bedallocation.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A bed in a room to which a stay can be assigned.")
public record BedDTO(
        @Schema(description = "Unique identifier of the bed.") String id,
        @Schema(description = "ID of the room the bed belongs to.") String roomId,
        @Schema(description = "Index of the bed within its room.") int indexInRoom) {

    @SuppressWarnings("PMD.NullAssignment")
    public BedDTO {
        roomId = roomId != null && roomId.isBlank() ? null : roomId;
    }

    public BedDTO withId(String id) {
        return new BedDTO(id, roomId, indexInRoom);
    }

    public BedDTO withRoomId(String roomId) {
        return new BedDTO(id, roomId, indexInRoom);
    }

    public BedDTO withIndexInRoom(int indexInRoom) {
        return new BedDTO(id, roomId, indexInRoom);
    }
}
