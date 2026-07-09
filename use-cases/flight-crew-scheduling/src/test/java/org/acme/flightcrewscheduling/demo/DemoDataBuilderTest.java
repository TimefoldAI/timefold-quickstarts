package org.acme.flightcrewscheduling.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.acme.flightcrewscheduling.dto.FlightCrewScheduleInput;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildData() {
        FlightCrewScheduleInput problem = DemoDataBuilder.builder()
                .setFlightCount(14)
                .setDayCount(5)
                .build();

        assertEquals(14, problem.flights().size());
        assertFalse(problem.airports().isEmpty());
        assertFalse(problem.employees().isEmpty());
        // At least four crew slots per flight.
        assertTrue(problem.flightAssignments().size() >= problem.flights().size() * 4);
        problem.flightAssignments().forEach(assignment -> {
            assertNotNull(assignment.id());
            assertEquals(null, assignment.employeeId());
        });
        problem.flights().forEach(flight -> {
            assertNotNull(flight.departureUTCDateTime());
            assertNotNull(flight.arrivalUTCDateTime());
        });
    }

    @Test
    void dayCountGreaterThanZero() {
        DemoDataBuilder builder = DemoDataBuilder.builder().setDayCount(0);
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void flightCountMustBeEvenAndAboveOne() {
        assertThrows(IllegalStateException.class, () -> DemoDataBuilder.builder().setFlightCount(0).build());
        assertThrows(IllegalStateException.class, () -> DemoDataBuilder.builder().setFlightCount(7).build());
    }
}
