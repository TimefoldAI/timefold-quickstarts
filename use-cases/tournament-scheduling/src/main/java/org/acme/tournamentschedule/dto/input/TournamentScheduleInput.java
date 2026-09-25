package org.acme.tournamentschedule.dto.input;

import static java.util.Collections.emptyList;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The tournament scheduling planning problem input.")
public record TournamentScheduleInput(
        @Schema(description = "Teams that have to be assigned to matches.", required = true,
                minItems = 1) List<TeamInputDTO> teams,
        @Schema(description = "Days a team is unavailable to play a match.") List<UnavailabilityInputDTO> unavailabilities,
        @Schema(description = "Match slots that must each be assigned a team.", required = true,
                minItems = 1) List<MatchInputDTO> matches)
        implements
            ModelInput {

    public TournamentScheduleInput {
        unavailabilities = unavailabilities != null ? unavailabilities : emptyList();
    }

    public TournamentScheduleInput withMatches(List<MatchInputDTO> matches) {
        return new TournamentScheduleInput(teams, unavailabilities, matches);
    }
}
