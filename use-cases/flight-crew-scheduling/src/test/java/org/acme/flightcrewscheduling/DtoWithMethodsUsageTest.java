package org.acme.flightcrewscheduling;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.acme.flightcrewscheduling.dto.AirportDTO;
import org.acme.flightcrewscheduling.dto.AirportIdDetail;
import org.acme.flightcrewscheduling.dto.EmployeeDTO;
import org.acme.flightcrewscheduling.dto.EmployeeIdDetail;
import org.acme.flightcrewscheduling.dto.FlightAssignmentDTO;
import org.acme.flightcrewscheduling.dto.FlightAssignmentIdDetail;
import org.acme.flightcrewscheduling.dto.FlightCrewScheduleConfigOverrides;
import org.acme.flightcrewscheduling.dto.FlightCrewScheduleInput;
import org.acme.flightcrewscheduling.dto.FlightCrewScheduleInputMetrics;
import org.acme.flightcrewscheduling.dto.FlightCrewScheduleOutput;
import org.acme.flightcrewscheduling.dto.FlightCrewScheduleOutputMetrics;
import org.acme.flightcrewscheduling.dto.FlightDTO;
import org.acme.flightcrewscheduling.dto.FlightIdDetail;
import org.junit.jupiter.api.Test;

class DtoWithMethodsUsageTest {

    @Test
    void allWithMethodsProduceUpdatedCopies() {
        var baseAirport = new AirportDTO("LHR", "London Heathrow");
        var updatedAirport = baseAirport.withId("JFK").withName("New York JFK");

        var baseEmployee = new EmployeeDTO("e1", "Amy Cole", "LHR", List.of("Pilot"), List.of("2024-01-01"));
        var updatedEmployee = baseEmployee.withId("e2")
                .withName("Beth Fox")
                .withHomeAirportId("JFK")
                .withSkills(List.of("Flight attendant"))
                .withUnavailableDays(List.of("2024-02-02"));

        var baseFlight = new FlightDTO("F1", "LHR", "2024-01-01T08:00", "JFK", "2024-01-01T16:00");
        var updatedFlight = baseFlight.withFlightNumber("F2")
                .withDepartureAirportId("JFK")
                .withDepartureUTCDateTime("2024-01-02T08:00")
                .withArrivalAirportId("LHR")
                .withArrivalUTCDateTime("2024-01-02T16:00");

        var baseAssignment = new FlightAssignmentDTO("1", "F1", 1, "Pilot", "");
        var updatedAssignment = baseAssignment.withId("2")
                .withFlightNumber("F2")
                .withIndexInFlight(3)
                .withRequiredSkill("Flight attendant")
                .withEmployeeId("e2");

        var updatedAirportIdDetail = new AirportIdDetail("LHR").withAirportId("JFK");
        var updatedEmployeeIdDetail = new EmployeeIdDetail("e1").withEmployeeId("e2");
        var updatedFlightIdDetail = new FlightIdDetail("F1").withFlightNumber("F2");
        var updatedAssignmentIdDetail = new FlightAssignmentIdDetail("1").withFlightAssignmentId("2");

        var updatedOverrides = new FlightCrewScheduleConfigOverrides()
                .withFirstAssignmentNotDepartingFromHomeWeight(10L)
                .withLastAssignmentNotArrivingAtHomeWeight(20L);

        var updatedInput = new FlightCrewScheduleInput(List.of(baseAirport), List.of(baseEmployee), List.of(baseFlight),
                List.of(baseAssignment))
                .withAirports(List.of(updatedAirport))
                .withEmployees(List.of(updatedEmployee))
                .withFlights(List.of(updatedFlight))
                .withFlightAssignments(List.of(updatedAssignment));

        var updatedOutput = new FlightCrewScheduleOutput(List.of(baseAirport), List.of(baseEmployee), List.of(baseFlight),
                List.of(baseAssignment), "0hard/0soft")
                .withAirports(List.of(updatedAirport))
                .withEmployees(List.of(updatedEmployee))
                .withFlights(List.of(updatedFlight))
                .withFlightAssignments(List.of(updatedAssignment))
                .withScore("0hard/-1soft");

        var updatedInputMetrics = new FlightCrewScheduleInputMetrics(1, 2, 3, 4)
                .withFlightAssignments(10)
                .withFlights(20)
                .withEmployees(30)
                .withAirports(40);

        var updatedOutputMetrics = new FlightCrewScheduleOutputMetrics(1, 2, 3)
                .withTotalAssignedFlightAssignments(10)
                .withTotalUnassignedFlightAssignments(20)
                .withTotalUsedEmployees(30);

        assertThat(updatedAirport.id()).isEqualTo("JFK");
        assertThat(updatedAirport.name()).isEqualTo("New York JFK");
        assertThat(updatedEmployee.id()).isEqualTo("e2");
        assertThat(updatedEmployee.name()).isEqualTo("Beth Fox");
        assertThat(updatedEmployee.homeAirportId()).isEqualTo("JFK");
        assertThat(updatedEmployee.skills()).containsExactly("Flight attendant");
        assertThat(updatedEmployee.unavailableDays()).containsExactly("2024-02-02");
        assertThat(updatedFlight.flightNumber()).isEqualTo("F2");
        assertThat(updatedFlight.departureAirportId()).isEqualTo("JFK");
        assertThat(updatedFlight.departureUTCDateTime()).isEqualTo("2024-01-02T08:00");
        assertThat(updatedFlight.arrivalAirportId()).isEqualTo("LHR");
        assertThat(updatedFlight.arrivalUTCDateTime()).isEqualTo("2024-01-02T16:00");
        assertThat(updatedAssignment.id()).isEqualTo("2");
        assertThat(updatedAssignment.flightNumber()).isEqualTo("F2");
        assertThat(updatedAssignment.indexInFlight()).isEqualTo(3);
        assertThat(updatedAssignment.requiredSkill()).isEqualTo("Flight attendant");
        assertThat(updatedAssignment.employeeId()).isEqualTo("e2");
        assertThat(updatedAirportIdDetail.airportId()).isEqualTo("JFK");
        assertThat(updatedEmployeeIdDetail.employeeId()).isEqualTo("e2");
        assertThat(updatedFlightIdDetail.flightNumber()).isEqualTo("F2");
        assertThat(updatedAssignmentIdDetail.flightAssignmentId()).isEqualTo("2");
        assertThat(updatedOverrides.firstAssignmentNotDepartingFromHomeWeight()).isEqualTo(10L);
        assertThat(updatedOverrides.lastAssignmentNotArrivingAtHomeWeight()).isEqualTo(20L);
        assertThat(updatedInput.airports()).containsExactly(updatedAirport);
        assertThat(updatedInput.employees()).containsExactly(updatedEmployee);
        assertThat(updatedInput.flights()).containsExactly(updatedFlight);
        assertThat(updatedInput.flightAssignments()).containsExactly(updatedAssignment);
        assertThat(updatedOutput.airports()).containsExactly(updatedAirport);
        assertThat(updatedOutput.employees()).containsExactly(updatedEmployee);
        assertThat(updatedOutput.flights()).containsExactly(updatedFlight);
        assertThat(updatedOutput.flightAssignments()).containsExactly(updatedAssignment);
        assertThat(updatedOutput.score()).isEqualTo("0hard/-1soft");
        assertThat(updatedInputMetrics.flightAssignments()).isEqualTo(10);
        assertThat(updatedInputMetrics.flights()).isEqualTo(20);
        assertThat(updatedInputMetrics.employees()).isEqualTo(30);
        assertThat(updatedInputMetrics.airports()).isEqualTo(40);
        assertThat(updatedOutputMetrics.totalAssignedFlightAssignments()).isEqualTo(10);
        assertThat(updatedOutputMetrics.totalUnassignedFlightAssignments()).isEqualTo(20);
        assertThat(updatedOutputMetrics.totalUsedEmployees()).isEqualTo(30);
    }
}
