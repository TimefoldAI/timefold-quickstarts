package org.acme.schooltimetabling.dto.output;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "The timeslot and room a lesson is scheduled in.")
public record LessonOutputDTO(
        @Schema(description = "Unique identifier of the lesson.", required = true, minLength = 1) String id,
        @Schema(description = "ID of the timeslot the lesson is scheduled in, or null if still unassigned.") String timeslotId,
        @Schema(description = "ID of the room the lesson is scheduled in, or null if still unassigned.") String roomId) {
}
