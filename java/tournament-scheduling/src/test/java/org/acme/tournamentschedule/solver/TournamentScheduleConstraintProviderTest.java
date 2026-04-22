package org.acme.tournamentschedule.solver;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.tournamentschedule.domain.Day;
import org.acme.tournamentschedule.domain.Team;
import org.acme.tournamentschedule.domain.TeamAssignment;
import org.acme.tournamentschedule.domain.TournamentSchedule;
import org.acme.tournamentschedule.domain.UnavailabilityPenalty;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class TournamentScheduleConstraintProviderTest {

    @Inject
    ConstraintVerifier<TournamentScheduleConstraintProvider, TournamentSchedule> constraintVerifier;

    private static final Day DAY0 = new Day(0);
    private static final Day DAY1 = new Day(1);
    private static final Day DAY2 = new Day(2);
    private static final Team TEAM0 = new Team(0, "A");
    private static final Team TEAM1 = new Team(1, "B");
    private static final Team TEAM2 = new Team(2, "C");

    @Test
    void oneAssignmentPerDayPerTeam() {
        TeamAssignment assignment1 = new TeamAssignment(0, DAY0, 0);
        assignment1.setTeam(TEAM0);
        TeamAssignment assignment2 = new TeamAssignment(1, DAY0, 1);
        assignment2.setTeam(TEAM0);
        TeamAssignment assignment3 = new TeamAssignment(2, DAY0, 2);
        assignment3.setTeam(TEAM0);
        TeamAssignment assignment4 = new TeamAssignment(3, DAY1, 0);
        assignment4.setTeam(TEAM1);
        TeamAssignment assignment5 = new TeamAssignment(4, DAY2, 1);
        assignment5.setTeam(TEAM1);

        constraintVerifier.verifyThat(TournamentScheduleConstraintProvider::oneAssignmentPerDatePerTeam)
                .given(assignment1, assignment2, assignment3, assignment4, assignment5, TEAM0, TEAM1, TEAM2)
                .penalizesBy(3); // TEAM0 by 2, TEAM1 by 1.
    }

    @Test
    void unavailabilityPenalty() {
        TeamAssignment assignment1 = new TeamAssignment(0, DAY0, 0);
        assignment1.setTeam(TEAM0);
        TeamAssignment assignment2 = new TeamAssignment(1, DAY1, 0);
        assignment2.setTeam(TEAM1);
        TeamAssignment assignment3 = new TeamAssignment(2, DAY1, 1);
        assignment3.setTeam(TEAM1);
        TeamAssignment assignment4 = new TeamAssignment(3, DAY2, 0);
        assignment4.setTeam(TEAM1);

        UnavailabilityPenalty unavailabilityPenalty1 = new UnavailabilityPenalty(TEAM0, DAY0);
        UnavailabilityPenalty unavailabilityPenalty2 = new UnavailabilityPenalty(TEAM1, DAY1);

        constraintVerifier.verifyThat(TournamentScheduleConstraintProvider::unavailabilityPenalty)
                .given(assignment1, assignment2, assignment3, assignment4, unavailabilityPenalty1, unavailabilityPenalty2)
                .penalizesBy(2); // TEAM0 by 1, TEAM1 by 1.
    }

    @Test
    void fairAssignmentCountPerTeam() {
        TeamAssignment assignment1 = new TeamAssignment(0, DAY0, 0);
        assignment1.setTeam(TEAM0);
        TeamAssignment assignment2 = new TeamAssignment(1, DAY1, 0);
        assignment2.setTeam(TEAM1);
        TeamAssignment assignment3 = new TeamAssignment(2, DAY2, 0);
        assignment3.setTeam(TEAM2);
        TeamAssignment assignment4 = new TeamAssignment(3, DAY0, 0);
        assignment4.setTeam(TEAM2);

        constraintVerifier.verifyThat(TournamentScheduleConstraintProvider::fairAssignmentCountPerTeam)
                .given(assignment1, assignment2, assignment3)
                .penalizesBy(0);
        // Team 2 twice while everyone else just once = more unfair.
        constraintVerifier.verifyThat(TournamentScheduleConstraintProvider::fairAssignmentCountPerTeam)
                .given(assignment1, assignment2, assignment3, assignment4)
                .penalizesByMoreThan(0);
    }

    @Test
    void evenlyConfrontationCount() {
        TeamAssignment assignment1 = new TeamAssignment(0, DAY0, 0);
        assignment1.setTeam(TEAM0);
        TeamAssignment assignment2 = new TeamAssignment(1, DAY0, 0);
        assignment2.setTeam(TEAM1);
        TeamAssignment assignment3 = new TeamAssignment(2, DAY0, 0);
        assignment3.setTeam(TEAM2);
        TeamAssignment assignment4 = new TeamAssignment(3, DAY0, 0);
        assignment4.setTeam(TEAM2);

        constraintVerifier.verifyThat(TournamentScheduleConstraintProvider::evenlyConfrontationCount)
                .given(assignment1, assignment2, assignment3)
                .penalizesBy(0);
        // Team 2 twice while everyone else just once = more unfair.
        constraintVerifier.verifyThat(TournamentScheduleConstraintProvider::evenlyConfrontationCount)
                .given(assignment1, assignment2, assignment3, assignment4)
                .penalizesByMoreThan(0);
    }
}
