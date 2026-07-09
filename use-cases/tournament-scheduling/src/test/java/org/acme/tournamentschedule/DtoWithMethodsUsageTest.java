package org.acme.tournamentschedule;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.acme.tournamentschedule.dto.DayDTO;
import org.acme.tournamentschedule.dto.TeamAssignmentDTO;
import org.acme.tournamentschedule.dto.TeamAssignmentIdDetail;
import org.acme.tournamentschedule.dto.TeamDTO;
import org.acme.tournamentschedule.dto.TeamIdDetail;
import org.acme.tournamentschedule.dto.TournamentScheduleConfigOverrides;
import org.acme.tournamentschedule.dto.TournamentScheduleInput;
import org.acme.tournamentschedule.dto.TournamentScheduleInputMetrics;
import org.acme.tournamentschedule.dto.TournamentScheduleOutput;
import org.acme.tournamentschedule.dto.TournamentScheduleOutputMetrics;
import org.acme.tournamentschedule.dto.UnavailabilityPenaltyDTO;
import org.junit.jupiter.api.Test;

class DtoWithMethodsUsageTest {

    @Test
    void allWithMethodsProduceUpdatedCopies() {
        var baseTeam = new TeamDTO("0", "Maarten");
        var updatedTeam = baseTeam.withId("1").withName("Geoffrey");

        var baseDay = new DayDTO(0);
        var updatedDay = baseDay.withDateIndex(5);

        var basePenalty = new UnavailabilityPenaltyDTO("0", 0);
        var updatedPenalty = basePenalty.withTeamId("1").withDateIndex(3);

        var baseAssignment = new TeamAssignmentDTO("0", 0, 0, false, "");
        var updatedAssignment = baseAssignment.withId("1")
                .withDateIndex(2)
                .withIndexInDay(3)
                .withPinned(true)
                .withTeamId("1");

        var updatedTeamIdDetail = new TeamIdDetail("0").withTeamId("1");
        var updatedAssignmentIdDetail = new TeamAssignmentIdDetail("0").withTeamAssignmentId("1");

        var updatedOverrides = new TournamentScheduleConfigOverrides()
                .withFairAssignmentCountPerTeamWeight(10L)
                .withEvenlyConfrontationCountWeight(20L);

        var updatedInput =
                new TournamentScheduleInput(List.of(baseTeam), List.of(baseDay), List.of(basePenalty),
                        List.of(baseAssignment))
                        .withTeams(List.of(updatedTeam))
                        .withDays(List.of(updatedDay))
                        .withUnavailabilityPenalties(List.of(updatedPenalty))
                        .withTeamAssignments(List.of(updatedAssignment));

        var updatedOutput =
                new TournamentScheduleOutput(List.of(baseTeam), List.of(baseDay), List.of(basePenalty),
                        List.of(baseAssignment), "0hard/0medium/0soft")
                        .withTeams(List.of(updatedTeam))
                        .withDays(List.of(updatedDay))
                        .withUnavailabilityPenalties(List.of(updatedPenalty))
                        .withTeamAssignments(List.of(updatedAssignment))
                        .withScore("1hard/0medium/0soft");

        var updatedInputMetrics = new TournamentScheduleInputMetrics(1, 2, 3, 4)
                .withTeams(10)
                .withDays(20)
                .withTeamAssignments(30)
                .withUnavailabilityPenalties(40);

        var updatedOutputMetrics = new TournamentScheduleOutputMetrics(1, 2, 3, 4)
                .withTotalAssignedTeamAssignments(10)
                .withTotalUnassignedTeamAssignments(20)
                .withTotalUsedTeams(30)
                .withTotalUsedDays(40);

        assertThat(updatedTeam.id()).isEqualTo("1");
        assertThat(updatedTeam.name()).isEqualTo("Geoffrey");
        assertThat(updatedDay.dateIndex()).isEqualTo(5);
        assertThat(updatedPenalty.teamId()).isEqualTo("1");
        assertThat(updatedPenalty.dateIndex()).isEqualTo(3);
        assertThat(updatedAssignment.id()).isEqualTo("1");
        assertThat(updatedAssignment.dateIndex()).isEqualTo(2);
        assertThat(updatedAssignment.indexInDay()).isEqualTo(3);
        assertThat(updatedAssignment.pinned()).isTrue();
        assertThat(updatedAssignment.teamId()).isEqualTo("1");
        assertThat(updatedTeamIdDetail.teamId()).isEqualTo("1");
        assertThat(updatedAssignmentIdDetail.teamAssignmentId()).isEqualTo("1");
        assertThat(updatedOverrides.fairAssignmentCountPerTeamWeight()).isEqualTo(10L);
        assertThat(updatedOverrides.evenlyConfrontationCountWeight()).isEqualTo(20L);
        assertThat(updatedInput.teams()).containsExactly(updatedTeam);
        assertThat(updatedInput.days()).containsExactly(updatedDay);
        assertThat(updatedInput.unavailabilityPenalties()).containsExactly(updatedPenalty);
        assertThat(updatedInput.teamAssignments()).containsExactly(updatedAssignment);
        assertThat(updatedOutput.teams()).containsExactly(updatedTeam);
        assertThat(updatedOutput.days()).containsExactly(updatedDay);
        assertThat(updatedOutput.unavailabilityPenalties()).containsExactly(updatedPenalty);
        assertThat(updatedOutput.teamAssignments()).containsExactly(updatedAssignment);
        assertThat(updatedOutput.score()).isEqualTo("1hard/0medium/0soft");
        assertThat(updatedInputMetrics.teams()).isEqualTo(10);
        assertThat(updatedInputMetrics.days()).isEqualTo(20);
        assertThat(updatedInputMetrics.teamAssignments()).isEqualTo(30);
        assertThat(updatedInputMetrics.unavailabilityPenalties()).isEqualTo(40);
        assertThat(updatedOutputMetrics.totalAssignedTeamAssignments()).isEqualTo(10);
        assertThat(updatedOutputMetrics.totalUnassignedTeamAssignments()).isEqualTo(20);
        assertThat(updatedOutputMetrics.totalUsedTeams()).isEqualTo(30);
        assertThat(updatedOutputMetrics.totalUsedDays()).isEqualTo(40);
    }
}
