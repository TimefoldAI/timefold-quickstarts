package org.acme.sportsleagueschedule.dto.output;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "A match that is either scheduled in a round, or not.")
public record MatchAssignmentDTO(
        @Schema(description = "Unique identifier of the match.", required = true, minLength = 1) String id,
        @Schema(description = "Index of the round the match is played in, or null if unscheduled.") Integer roundIndex) {
}
