package org.acme.meetingschedule.dto.output;

import java.time.OffsetDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "A meeting that is either assigned a room and a start, or not.")
public record MeetingOutputDTO(
        @Schema(description = "Unique identifier of the meeting.", required = true, minLength = 1) String id,
        @Schema(description = "ID of the room this meeting is held in, or null if unassigned.") String roomId,
        @Schema(description = "When the meeting starts, in ISO-8601 date and time format with an offset, or null if "
                + "unassigned.") OffsetDateTime startDateTime) {
}
