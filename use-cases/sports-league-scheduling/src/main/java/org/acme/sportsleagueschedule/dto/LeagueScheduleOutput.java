package org.acme.sportsleagueschedule.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The sports league scheduling planning problem output.")
public record LeagueScheduleOutput(
        @Schema(description = "Rounds on which matches can be scheduled.") List<RoundDTO> rounds,
        @Schema(description = "Teams participating in the league.") List<TeamDTO> teams,
        @Schema(description = "Matches with their assigned round.") List<MatchDTO> matches,
        @Schema(description = "The score of the solution.") String score) implements ModelOutput {

    public LeagueScheduleOutput {
        rounds = List.copyOf(rounds);
        teams = List.copyOf(teams);
        matches = List.copyOf(matches);
    }

    public LeagueScheduleOutput withRounds(List<RoundDTO> rounds) {
        return new LeagueScheduleOutput(rounds, teams, matches, score);
    }

    public LeagueScheduleOutput withTeams(List<TeamDTO> teams) {
        return new LeagueScheduleOutput(rounds, teams, matches, score);
    }

    public LeagueScheduleOutput withMatches(List<MatchDTO> matches) {
        return new LeagueScheduleOutput(rounds, teams, matches, score);
    }

    public LeagueScheduleOutput withScore(String score) {
        return new LeagueScheduleOutput(rounds, teams, matches, score);
    }
}
