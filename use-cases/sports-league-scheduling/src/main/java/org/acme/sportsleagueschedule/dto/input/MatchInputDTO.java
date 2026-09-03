package org.acme.sportsleagueschedule.dto.input;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A single match between two teams, to be assigned to a round.")
public record MatchInputDTO(
        @Schema(description = "Unique identifier of the match.", required = true, minLength = 1) String id,
        @Schema(description = "ID of the team playing at its own venue.", required = true,
                minLength = 1) String homeTeamId,
        @Schema(description = "ID of the team travelling to the home team's venue.", required = true,
                minLength = 1) String awayTeamId,
        @Schema(description = "Whether this is a classic match, such as a derby, that should be played on a "
                + "weekend or holiday round.", required = true) Boolean classicMatch,
        @Schema(description = "Index of the round this match is scheduled in, or null if unscheduled.",
                minimum = "0") Integer roundIndex) {

    public MatchInputDTO withRoundIndex(Integer roundIndex) {
        return new MatchInputDTO(id, homeTeamId, awayTeamId, classicMatch, roundIndex);
    }
}
