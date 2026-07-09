package org.acme.meetingschedule.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The assignment of a meeting to a starting time grain and a room.")
public record MeetingAssignmentDTO(
        @Schema(description = "Unique identifier of the meeting assignment.") String id,
        @Schema(description = "ID of the meeting being assigned.") String meetingId,
        @Schema(description = "ID of the starting time grain. Null when unassigned.") String startingTimeGrainId,
        @Schema(description = "ID of the assigned room. Null when unassigned.") String roomId,
        @Schema(description = "Whether the assignment is pinned and must not be changed.") boolean pinned) {

    public MeetingAssignmentDTO {
        startingTimeGrainId = normalizeId(startingTimeGrainId);
        roomId = normalizeId(roomId);
    }

    private static String normalizeId(String id) {
        return id != null && id.isBlank() ? null : id;
    }

    public MeetingAssignmentDTO withId(String id) {
        return new MeetingAssignmentDTO(id, meetingId, startingTimeGrainId, roomId, pinned);
    }

    public MeetingAssignmentDTO withMeetingId(String meetingId) {
        return new MeetingAssignmentDTO(id, meetingId, startingTimeGrainId, roomId, pinned);
    }

    public MeetingAssignmentDTO withStartingTimeGrainId(String startingTimeGrainId) {
        return new MeetingAssignmentDTO(id, meetingId, startingTimeGrainId, roomId, pinned);
    }

    public MeetingAssignmentDTO withRoomId(String roomId) {
        return new MeetingAssignmentDTO(id, meetingId, startingTimeGrainId, roomId, pinned);
    }

    public MeetingAssignmentDTO withPinned(boolean pinned) {
        return new MeetingAssignmentDTO(id, meetingId, startingTimeGrainId, roomId, pinned);
    }
}
