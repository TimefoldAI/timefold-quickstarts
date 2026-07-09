package org.acme.tournamentschedule.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.acme.tournamentschedule.dto.TournamentScheduleInput;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildData() {
        TournamentScheduleInput problem = correctBuilder().build();

        assertEquals(7, problem.teams().size());
        assertEquals(18, problem.days().size());
        assertEquals(12, problem.unavailabilityPenalties().size());
        assertEquals(18 * 4, problem.teamAssignments().size());
        problem.teamAssignments().forEach(assignment -> {
            assertNotNull(assignment.id());
            assertEquals(null, assignment.teamId());
        });
    }

    @Test
    void dayCountGreaterThanZero() {
        DemoDataBuilder builder = correctBuilder().setDayCount(0);
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void assignmentsPerDayGreaterThanZero() {
        DemoDataBuilder builder = correctBuilder().setAssignmentsPerDay(0);
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void atLeastOneTeam() {
        DemoDataBuilder builder = DemoDataBuilder.builder().setDayCount(18).setAssignmentsPerDay(4);
        assertThrows(IllegalStateException.class, builder::build);
    }

    static DemoDataBuilder correctBuilder() {
        return DemoDataBuilder.builder()
                .setDayCount(18)
                .setAssignmentsPerDay(4)
                .setUnavailabilityPenaltyCount(12)
                .setRandomSeed(0L)
                .addTeam("Maarten")
                .addTeam("Geoffrey")
                .addTeam("Lukas")
                .addTeam("Chris")
                .addTeam("Fred")
                .addTeam("Radek")
                .addTeam("Maciej");
    }
}
