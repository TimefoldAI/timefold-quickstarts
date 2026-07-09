package org.acme.tournamentschedule.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The tournament scheduling planning problem input.")
public record TournamentScheduleInput(
        @Schema(description = "List of teams competing in the tournament.") List<TeamDTO> teams,
        @Schema(description = "List of days on which assignments take place.") List<DayDTO> days,
        @Schema(description = "List of unavailability penalties.") List<UnavailabilityPenaltyDTO> unavailabilityPenalties,
        @Schema(description = "List of team assignment slots to be filled.") List<TeamAssignmentDTO> teamAssignments)
        implements
            ModelInput {

    public TournamentScheduleInput {
        teams = List.copyOf(teams);
        days = List.copyOf(days);
        unavailabilityPenalties = List.copyOf(unavailabilityPenalties);
        teamAssignments = List.copyOf(teamAssignments);
    }

    public TournamentScheduleInput withTeams(List<TeamDTO> teams) {
        return new TournamentScheduleInput(teams, days, unavailabilityPenalties, teamAssignments);
    }

    public TournamentScheduleInput withDays(List<DayDTO> days) {
        return new TournamentScheduleInput(teams, days, unavailabilityPenalties, teamAssignments);
    }

    public TournamentScheduleInput withUnavailabilityPenalties(List<UnavailabilityPenaltyDTO> unavailabilityPenalties) {
        return new TournamentScheduleInput(teams, days, unavailabilityPenalties, teamAssignments);
    }

    public TournamentScheduleInput withTeamAssignments(List<TeamAssignmentDTO> teamAssignments) {
        return new TournamentScheduleInput(teams, days, unavailabilityPenalties, teamAssignments);
    }
}
