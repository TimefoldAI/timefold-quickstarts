package org.acme.tournamentschedule.dto.input;

import java.time.LocalDate;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A match slot that must be assigned a team, or already is.")
public record MatchInputDTO(
        @Schema(description = "Unique identifier of the match.", required = true, minLength = 1) String id,
        @Schema(description = "The day the match is held on, in ISO-8601 date format.",
                required = true) LocalDate date,
        @Schema(description = "ID of the team playing this match, or null if unassigned.",
                minLength = 1) String teamId,
        @Schema(description = "Whether this match's team is pinned and must not be changed by the "
                + "solver.") Boolean pinned) {

    public MatchInputDTO withTeamId(String teamId) {
        return new MatchInputDTO(id, date, teamId, pinned);
    }
}
