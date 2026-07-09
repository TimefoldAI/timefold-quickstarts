package org.acme.tournamentschedule.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The tournament scheduling planning problem output.")
public record TournamentScheduleOutput(
        @Schema(description = "List of teams competing in the tournament.") List<TeamDTO> teams,
        @Schema(description = "List of days on which assignments take place.") List<DayDTO> days,
        @Schema(description = "List of unavailability penalties.") List<UnavailabilityPenaltyDTO> unavailabilityPenalties,
        @Schema(description = "List of team assignments with their assigned team.") List<TeamAssignmentDTO> teamAssignments,
        @Schema(description = "The score of the solution.") String score) implements ModelOutput {

    public TournamentScheduleOutput {
        teams = List.copyOf(teams);
        days = List.copyOf(days);
        unavailabilityPenalties = List.copyOf(unavailabilityPenalties);
        teamAssignments = List.copyOf(teamAssignments);
    }

    public TournamentScheduleOutput withTeams(List<TeamDTO> teams) {
        return new TournamentScheduleOutput(teams, days, unavailabilityPenalties, teamAssignments, score);
    }

    public TournamentScheduleOutput withDays(List<DayDTO> days) {
        return new TournamentScheduleOutput(teams, days, unavailabilityPenalties, teamAssignments, score);
    }

    public TournamentScheduleOutput withUnavailabilityPenalties(List<UnavailabilityPenaltyDTO> unavailabilityPenalties) {
        return new TournamentScheduleOutput(teams, days, unavailabilityPenalties, teamAssignments, score);
    }

    public TournamentScheduleOutput withTeamAssignments(List<TeamAssignmentDTO> teamAssignments) {
        return new TournamentScheduleOutput(teams, days, unavailabilityPenalties, teamAssignments, score);
    }

    public TournamentScheduleOutput withScore(String score) {
        return new TournamentScheduleOutput(teams, days, unavailabilityPenalties, teamAssignments, score);
    }
}
