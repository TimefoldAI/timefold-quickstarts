package org.acme.tournamentschedule.solver;

import static org.acme.tournamentschedule.support.TestHelper.aTeam;
import static org.acme.tournamentschedule.support.TestHelper.anAssignment;
import static org.acme.tournamentschedule.support.TestHelper.anUnavailabilityPenalty;

import java.time.LocalDate;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.tournamentschedule.domain.TournamentSchedule;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class TournamentScheduleConstraintProviderTest {

    @Inject
    ConstraintVerifier<TournamentScheduleConstraintProvider, TournamentSchedule> constraintVerifier;

    private static final LocalDate DAY0 = LocalDate.of(2024, 1, 1);
    private static final LocalDate DAY1 = LocalDate.of(2024, 1, 2);
    private static final LocalDate DAY2 = LocalDate.of(2024, 1, 3);

    @Test
    void oneAssignmentPerDatePerTeamUnpenalized() {
        var team0 = aTeam("T0");
        var team1 = aTeam("T1");
        var assignment1 = anAssignment("0", DAY0).team(team0).build();
        var assignment2 = anAssignment("1", DAY0).team(team1).build();
        var assignment3 = anAssignment("2", DAY1).team(team0).build();

        constraintVerifier.verifyThat(TournamentScheduleConstraintProvider::oneAssignmentPerDatePerTeam)
                .given(assignment1, assignment2, assignment3)
                .penalizesBy(0);
    }

    @Test
    void oneAssignmentPerDatePerTeamPenalized() {
        var team0 = aTeam("T0");
        var team1 = aTeam("T1");
        var assignment1 = anAssignment("0", DAY0).team(team0).build();
        var assignment2 = anAssignment("1", DAY0).team(team0).build();
        var assignment3 = anAssignment("2", DAY0).team(team0).build();
        var assignment4 = anAssignment("3", DAY1).team(team1).build();
        var assignment5 = anAssignment("4", DAY2).team(team1).build();

        constraintVerifier.verifyThat(TournamentScheduleConstraintProvider::oneAssignmentPerDatePerTeam)
                .given(assignment1, assignment2, assignment3, assignment4, assignment5)
                .penalizesBy(3); // T0 by 2 (3 assignments on DAY0 -> 3 pairs), T1 by 0 (different days).
    }

    @Test
    void unavailabilityPenaltyUnpenalized() {
        var team0 = aTeam("T0");
        var team1 = aTeam("T1");
        var assignment1 = anAssignment("0", DAY0).team(team0).build();
        var assignment2 = anAssignment("1", DAY1).team(team1).build();
        var unavailabilityPenalty = anUnavailabilityPenalty(team0, DAY1);

        constraintVerifier.verifyThat(TournamentScheduleConstraintProvider::unavailabilityPenalty)
                .given(assignment1, assignment2, unavailabilityPenalty)
                .penalizesBy(0);
    }

    @Test
    void unavailabilityPenaltyPenalized() {
        var team0 = aTeam("T0");
        var team1 = aTeam("T1");
        var assignment1 = anAssignment("0", DAY0).team(team0).build();
        var assignment2 = anAssignment("1", DAY1).team(team1).build();
        var assignment3 = anAssignment("2", DAY1).team(team1).build();
        var unavailabilityPenalty1 = anUnavailabilityPenalty(team0, DAY0);
        var unavailabilityPenalty2 = anUnavailabilityPenalty(team1, DAY1);

        constraintVerifier.verifyThat(TournamentScheduleConstraintProvider::unavailabilityPenalty)
                .given(assignment1, assignment2, assignment3, unavailabilityPenalty1, unavailabilityPenalty2)
                .penalizesBy(2); // T0 by 1, T1 by 1.
    }

    @Test
    void fairAssignmentCountPerTeamUnpenalized() {
        var team0 = aTeam("T0");
        var team1 = aTeam("T1");
        var team2 = aTeam("T2");
        var assignment1 = anAssignment("0", DAY0).team(team0).build();
        var assignment2 = anAssignment("1", DAY1).team(team1).build();
        var assignment3 = anAssignment("2", DAY2).team(team2).build();

        constraintVerifier.verifyThat(TournamentScheduleConstraintProvider::fairAssignmentCountPerTeam)
                .given(team0.build(), team1.build(), team2.build(), assignment1, assignment2, assignment3)
                .penalizesBy(0);
    }

    @Test
    void fairAssignmentCountPerTeamPenalized() {
        var team0 = aTeam("T0");
        var team1 = aTeam("T1");
        var team2 = aTeam("T2");
        var assignment1 = anAssignment("0", DAY0).team(team0).build();
        var assignment2 = anAssignment("1", DAY1).team(team1).build();
        var assignment3 = anAssignment("2", DAY2).team(team2).build();
        var assignment4 = anAssignment("3", DAY0).team(team2).build();

        // Team 2 twice while everyone else just once = more unfair.
        constraintVerifier.verifyThat(TournamentScheduleConstraintProvider::fairAssignmentCountPerTeam)
                .given(team0.build(), team1.build(), team2.build(), assignment1, assignment2, assignment3, assignment4)
                .penalizesByMoreThan(0);
    }

    @Test
    void fairAssignmentCountPerTeamPenalizesTeamWithoutAssignments() {
        var team0 = aTeam("T0");
        var team1 = aTeam("T1");
        var team2 = aTeam("T2");
        var assignment1 = anAssignment("0", DAY0).team(team0).build();
        var assignment2 = anAssignment("1", DAY1).team(team1).build();

        // Team 2 never plays. The teams that do play have one match each, so the schedule only looks unbalanced
        // because every team is fed into the load balance, including the ones without a single assignment.
        constraintVerifier.verifyThat(TournamentScheduleConstraintProvider::fairAssignmentCountPerTeam)
                .given(team0.build(), team1.build(), team2.build(), assignment1, assignment2)
                .penalizesByMoreThan(0);
    }

    @Test
    void evenlyConfrontationCountUnpenalized() {
        var team0 = aTeam("T0");
        var team1 = aTeam("T1");
        var team2 = aTeam("T2");
        var assignment1 = anAssignment("0", DAY0).team(team0).build();
        var assignment2 = anAssignment("1", DAY0).team(team1).build();
        var assignment3 = anAssignment("2", DAY0).team(team2).build();

        // Every one of the three possible pairings happens exactly once.
        constraintVerifier.verifyThat(TournamentScheduleConstraintProvider::evenlyConfrontationCount)
                .given(team0.build(), team1.build(), team2.build(), assignment1, assignment2, assignment3)
                .penalizesBy(0);
    }

    @Test
    void evenlyConfrontationCountPenalized() {
        var team0 = aTeam("T0");
        var team1 = aTeam("T1");
        var team2 = aTeam("T2");
        var assignment1 = anAssignment("0", DAY0).team(team0).build();
        var assignment2 = anAssignment("1", DAY0).team(team1).build();
        var assignment3 = anAssignment("2", DAY0).team(team2).build();
        var assignment4 = anAssignment("3", DAY0).team(team2).build();

        // Team 0 and team 2 confront twice while every other pair confronts once = more unfair.
        constraintVerifier.verifyThat(TournamentScheduleConstraintProvider::evenlyConfrontationCount)
                .given(team0.build(), team1.build(), team2.build(), assignment1, assignment2, assignment3, assignment4)
                .penalizesByMoreThan(0);
    }

    @Test
    void evenlyConfrontationCountPenalizesPairThatNeverConfronts() {
        var team0 = aTeam("T0");
        var team1 = aTeam("T1");
        var team2 = aTeam("T2");
        var assignment1 = anAssignment("0", DAY0).team(team0).build();
        var assignment2 = anAssignment("1", DAY0).team(team1).build();
        var assignment3 = anAssignment("2", DAY1).team(team0).build();
        var assignment4 = anAssignment("3", DAY1).team(team1).build();

        // Team 0 and team 1 replay each other while team 2 never meets either of them. The pairs that do confront
        // are perfectly balanced, so only the two pairings that never happen make this uneven.
        constraintVerifier.verifyThat(TournamentScheduleConstraintProvider::evenlyConfrontationCount)
                .given(team0.build(), team1.build(), team2.build(), assignment1, assignment2, assignment3, assignment4)
                .penalizesByMoreThan(0);
    }
}
