package org.acme.tournamentschedule.support;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.acme.tournamentschedule.domain.Team;
import org.acme.tournamentschedule.domain.TeamAssignment;
import org.acme.tournamentschedule.domain.UnavailabilityPenalty;
import org.acme.tournamentschedule.dto.input.MatchInputDTO;
import org.acme.tournamentschedule.dto.input.TeamInputDTO;
import org.acme.tournamentschedule.dto.input.TournamentScheduleInput;
import org.acme.tournamentschedule.dto.input.UnavailabilityInputDTO;

// To keep our production classes as simple as possible, we've added these methods to help construct the data needed for testing.
public final class TestHelper {

    private static final LocalDate DEFAULT_DATE = LocalDate.of(2024, 1, 1);

    private TestHelper() {
    }

    // ************************************************************************
    // Solver model
    // ************************************************************************

    public static TeamBuilder aTeam(String id) {
        return new TeamBuilder(id);
    }

    public static UnavailabilityPenalty anUnavailabilityPenalty(TeamBuilder team, LocalDate date) {
        return new UnavailabilityPenalty(team.build(), date);
    }

    public static TeamAssignmentBuilder anAssignment(String id, LocalDate date) {
        return new TeamAssignmentBuilder(id, date);
    }

    public static final class TeamBuilder {

        private final String id;
        private String name;

        private TeamBuilder(String id) {
            this.id = id;
            this.name = "Team " + id;
        }

        public TeamBuilder name(String name) {
            this.name = name;
            return this;
        }

        public Team build() {
            return new Team(id, name);
        }
    }

    public static final class TeamAssignmentBuilder {

        private final String id;
        private final LocalDate date;
        private TeamBuilder team;
        private boolean pinned;

        private TeamAssignmentBuilder(String id, LocalDate date) {
            this.id = id;
            this.date = date;
        }

        public TeamAssignmentBuilder team(TeamBuilder team) {
            this.team = team;
            return this;
        }

        public TeamAssignmentBuilder pinned(boolean pinned) {
            this.pinned = pinned;
            return this;
        }

        public TeamAssignment build() {
            return new TeamAssignment(id, date, team == null ? null : team.build(), pinned);
        }
    }

    // ************************************************************************
    // Input DTOs
    // ************************************************************************

    public static TournamentScheduleInput input(List<TeamInputDTO> teams,
            List<UnavailabilityInputDTO> unavailabilities, List<MatchInputDTO> matches) {
        return new TournamentScheduleInput(teams, unavailabilities, matches);
    }

    public static TeamDTOBuilder aTeamDTO(String id) {
        return new TeamDTOBuilder(id);
    }

    public static MatchDTOBuilder aMatchDTO(String id) {
        return new MatchDTOBuilder(id);
    }

    public static UnavailabilityDTOBuilder anUnavailabilityDTO(String teamId) {
        return new UnavailabilityDTOBuilder(teamId);
    }

    /**
     * A small, deliberately conflict-free problem: 4 teams share 8 match slots over 4 days (2 a day), so every
     * team gets exactly 2 matches and no team ever has to play twice on the same day. Both a feasible score and a
     * zero medium score are within easy reach of the solver.
     */
    public static TournamentScheduleInput createProblem() {
        List<TeamInputDTO> teams = List.of(aTeamDTO("T1").build(), aTeamDTO("T2").build(), aTeamDTO("T3").build(),
                aTeamDTO("T4").build());
        List<MatchInputDTO> matches = new ArrayList<>();
        int matchNumber = 0;
        for (int dayOffset = 0; dayOffset < 4; dayOffset++) {
            LocalDate date = DEFAULT_DATE.plusDays(dayOffset);
            matches.add(aMatchDTO("M%d".formatted(++matchNumber)).date(date).build());
            matches.add(aMatchDTO("M%d".formatted(++matchNumber)).date(date).build());
        }
        return input(teams, List.of(), matches);
    }

    public static final class TeamDTOBuilder {

        private final String id;
        private String name;

        private TeamDTOBuilder(String id) {
            this.id = id;
            this.name = "Team " + id;
        }

        public TeamDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public TeamInputDTO build() {
            return new TeamInputDTO(id, name);
        }
    }

    public static final class MatchDTOBuilder {

        private final String id;
        private LocalDate date = DEFAULT_DATE;
        private String teamId;
        private Boolean pinned = false;

        private MatchDTOBuilder(String id) {
            this.id = id;
        }

        public MatchDTOBuilder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public MatchDTOBuilder teamId(String teamId) {
            this.teamId = teamId;
            return this;
        }

        public MatchDTOBuilder pinned(Boolean pinned) {
            this.pinned = pinned;
            return this;
        }

        public MatchInputDTO build() {
            return new MatchInputDTO(id, date, teamId, pinned);
        }
    }

    public static final class UnavailabilityDTOBuilder {

        private final String teamId;
        private LocalDate date = DEFAULT_DATE;

        private UnavailabilityDTOBuilder(String teamId) {
            this.teamId = teamId;
        }

        public UnavailabilityDTOBuilder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public UnavailabilityInputDTO build() {
            return new UnavailabilityInputDTO(teamId, date);
        }
    }
}
