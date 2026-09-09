package org.acme.sportsleagueschedule.dto.input;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The sports league scheduling problem input.")
public record LeagueScheduleInput(
        @Schema(description = "Matchdays the matches can be played on.", required = true,
                minItems = 1) List<RoundInputDTO> rounds,
        @Schema(description = "Teams competing in the league.", required = true,
                minItems = 2) List<TeamInputDTO> teams,
        @Schema(description = "Matches that must each be assigned to a round.", required = true,
                minItems = 1) List<MatchInputDTO> matches)
        implements
            ModelInput {

    public LeagueScheduleInput withMatches(List<MatchInputDTO> matches) {
        return new LeagueScheduleInput(rounds, teams, matches);
    }
}
