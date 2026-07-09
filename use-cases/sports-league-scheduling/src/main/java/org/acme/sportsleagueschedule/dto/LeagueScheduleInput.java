package org.acme.sportsleagueschedule.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The sports league scheduling planning problem input.")
public record LeagueScheduleInput(
        @Schema(description = "Rounds on which matches can be scheduled.") List<RoundDTO> rounds,
        @Schema(description = "Teams participating in the league.") List<TeamDTO> teams,
        @Schema(description = "Matches that must each be scheduled on a round.") List<MatchDTO> matches)
        implements
            ModelInput {

    public LeagueScheduleInput {
        rounds = List.copyOf(rounds);
        teams = List.copyOf(teams);
        matches = List.copyOf(matches);
    }

    public LeagueScheduleInput withRounds(List<RoundDTO> rounds) {
        return new LeagueScheduleInput(rounds, teams, matches);
    }

    public LeagueScheduleInput withTeams(List<TeamDTO> teams) {
        return new LeagueScheduleInput(rounds, teams, matches);
    }

    public LeagueScheduleInput withMatches(List<MatchDTO> matches) {
        return new LeagueScheduleInput(rounds, teams, matches);
    }
}
