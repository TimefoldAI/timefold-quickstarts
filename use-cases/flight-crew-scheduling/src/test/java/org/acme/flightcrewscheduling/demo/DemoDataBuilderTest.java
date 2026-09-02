package org.acme.flightcrewscheduling.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.acme.flightcrewscheduling.dto.input.FlightCrewScheduleInput;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildData() {
        FlightCrewScheduleInput problem = DemoDataBuilder.basic();

        assertThat(problem.airports()).hasSize(6);
        assertThat(problem.employees()).hasSize(48);
        assertThat(problem.flights()).hasSize(46);
        assertThat(problem.flightAssignments()).hasSize(190);

        assertThat(problem.employees()).allSatisfy(employee -> {
            assertThat(employee.id()).isNotNull();
            assertThat(employee.skills()).hasSize(1);
            assertThat(employee.homeAirportCode()).isIn("LHR", "BRU");
        });
        assertThat(problem.flights()).allSatisfy(flight -> assertThat(flight.arrivalUTCDateTime())
                .isAfter(flight.departureUTCDateTime()));
        assertThat(problem.flightAssignments()).allSatisfy(flightAssignment -> {
            assertThat(flightAssignment.requiredSkill()).isIn(DemoDataBuilder.PILOT_SKILL,
                    DemoDataBuilder.ATTENDANT_SKILL);
            assertThat(flightAssignment.employeeId()).isNull();
        });
    }
}
