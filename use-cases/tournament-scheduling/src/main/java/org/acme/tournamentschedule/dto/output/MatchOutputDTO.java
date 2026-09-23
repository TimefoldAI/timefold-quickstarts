package org.acme.tournamentschedule.dto.output;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "A match slot that is either assigned a team, or not.")
public record MatchOutputDTO(
        @Schema(description = "Unique identifier of the match.", required = true, minLength = 1) String id,
        @Schema(description = "ID of the team playing this match, or null if unassigned.") String teamId) {
}
