package org.acme.schooltimetabling.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a room ID validation issue.")
public record RoomIdDetail(
        @Schema(description = "The ID of the room.") String roomId) implements IssueMetadata {

    public RoomIdDetail {
        roomId = roomId == null ? "" : roomId;
    }

    public RoomIdDetail withRoomId(String roomId) {
        return new RoomIdDetail(roomId);
    }

    @Override
    public String getType() {
        return "RoomId";
    }
}
