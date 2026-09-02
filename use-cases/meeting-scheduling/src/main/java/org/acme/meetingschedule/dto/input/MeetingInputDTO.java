package org.acme.meetingschedule.dto.input;

import static java.util.Collections.emptyList;

import java.time.OffsetDateTime;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A meeting that must be assigned a room and a start.")
public record MeetingInputDTO(
        @Schema(description = "Unique identifier of the meeting.", required = true, minLength = 1) String id,
        @Schema(description = "Subject of the meeting.", required = true, minLength = 1) String topic,
        @Schema(description = "How long the meeting takes, in minutes. Must be a whole number of the slots the "
                + "office hours are divided into.", required = true, minimum = "1") Integer durationInMinutes,
        @Schema(description = "IDs of the people who must attend this meeting.") List<String> requiredAttendeeIds,
        @Schema(description = "IDs of the people who would like to attend this meeting.") List<String> preferredAttendeeIds,
        @Schema(description = "ID of the room this meeting is held in, or null if unassigned.",
                minLength = 1) String roomId,
        @Schema(description = "When the meeting starts, in ISO-8601 date and time format with an offset, or null if "
                + "unassigned. Must be the start of one of the slots the office hours are divided into.") OffsetDateTime startDateTime,
        @Schema(description = "Whether this meeting's room and start are pinned and must not be changed by the "
                + "solver.") Boolean pinned) {

    public MeetingInputDTO {
        requiredAttendeeIds = requiredAttendeeIds != null ? requiredAttendeeIds : emptyList();
        preferredAttendeeIds = preferredAttendeeIds != null ? preferredAttendeeIds : emptyList();
    }

    public MeetingInputDTO withAssignment(String roomId, OffsetDateTime startDateTime) {
        return new MeetingInputDTO(id, topic, durationInMinutes, requiredAttendeeIds, preferredAttendeeIds, roomId,
                startDateTime, pinned);
    }
}
