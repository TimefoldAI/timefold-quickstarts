package org.acme.conferencescheduling.dto.output;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "A talk that is either assigned to a timeslot and room, or not.")
public record TalkAssignmentDTO(
        @Schema(description = "Unique code of the talk.", required = true, minLength = 1) String code,
        @Schema(description = "ID of the timeslot this talk is assigned to, or null if unassigned.") String timeslotId,
        @Schema(description = "ID of the room this talk is assigned to, or null if unassigned.") String roomId) {
}
