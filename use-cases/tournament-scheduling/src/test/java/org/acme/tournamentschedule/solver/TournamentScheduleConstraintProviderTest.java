package org.acme.tournamentschedule.solver;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.tournamentschedule.domain.Day;
import org.acme.tournamentschedule.domain.Team;
import org.acme.tournamentschedule.domain.TeamAssignment;
import org.acme.tournamentschedule.domain.TournamentSchedule;
import org.acme.tournamentschedule.domain.UnavailabilityPenalty;
import org.junit.jupiter.api.Test;

class TournamentScheduleConstraintProviderTest {

    private static final Team TEAM1 = new Team("1", "Team1");
    private static final Team TEAM2 = new Team("2", "Team2");
    private static final Team TEAM3 = new Team("3", "Team3");
    private static final Day DAY1 = new Day(0);
    private static final Day DAY2 = new Day(1);

    private final ConstraintVerifier<TournamentScheduleConstraintProvider, TournamentSchedule> constraintVerifier =
            ConstraintVerifier.build(new TournamentScheduleConstraintProvider(), TournamentSchedule.class,
                    TeamAssignment.class);

    @Test
    void oneAssignmentPerDatePerTeam() {
        TeamAssignment first = new TeamAssignment("1", DAY1, 0, TEAM1);
        TeamAssignment conflicting = new TeamAssignment("2", DAY1, 1, TEAM1);
        TeamAssignment nonConflicting = new TeamAssignment("3", DAY2, 0, TEAM1);
        constraintVerifier.verifyThat(TournamentScheduleConstraintProvider::oneAssignmentPerDatePerTeam)
                .given(first, conflicting, nonConflicting)
                .penalizesBy(1);
    }

    @Test
    void unavailabilityPenalty() {
        TeamAssignment assignment = new TeamAssignment("1", DAY1, 0, TEAM1);
        UnavailabilityPenalty violated = new UnavailabilityPenalty(TEAM1, DAY1);
        UnavailabilityPenalty satisfied = new UnavailabilityPenalty(TEAM1, DAY2);
        constraintVerifier.verifyThat(TournamentScheduleConstraintProvider::unavailabilityPenalty)
                .given(assignment, violated, satisfied)
                .penalizesBy(1);
    }

    @Test
    void fairAssignmentCountPerTeam() {
        TeamAssignment a1 = new TeamAssignment("1", DAY1, 0, TEAM1);
        TeamAssignment a2 = new TeamAssignment("2", DAY1, 1, TEAM1);
        TeamAssignment a3 = new TeamAssignment("3", DAY2, 0, TEAM2);
        constraintVerifier.verifyThat(TournamentScheduleConstraintProvider::fairAssignmentCountPerTeam)
                .given(a1, a2, a3)
                .penalizesByMoreThan(0L);
    }

    @Test
    void evenlyConfrontationCount() {
        Day day3 = new Day(2);
        // Team1 confronts Team2 on two days but Team3 only once: an unfair distribution of confrontations.
        TeamAssignment a1 = new TeamAssignment("1", DAY1, 0, TEAM1);
        TeamAssignment a2 = new TeamAssignment("2", DAY1, 1, TEAM2);
        TeamAssignment a3 = new TeamAssignment("3", DAY2, 0, TEAM1);
        TeamAssignment a4 = new TeamAssignment("4", DAY2, 1, TEAM2);
        TeamAssignment a5 = new TeamAssignment("5", day3, 0, TEAM1);
        TeamAssignment a6 = new TeamAssignment("6", day3, 1, TEAM3);
        constraintVerifier.verifyThat(TournamentScheduleConstraintProvider::evenlyConfrontationCount)
                .given(a1, a2, a3, a4, a5, a6)
                .penalizesByMoreThan(0L);
    }
}
