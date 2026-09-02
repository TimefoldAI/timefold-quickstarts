package org.acme.flightcrewscheduling.solver;

import static org.acme.flightcrewscheduling.support.TestHelper.ATTENDANT_SKILL;
import static org.acme.flightcrewscheduling.support.TestHelper.FIRST_DAY;
import static org.acme.flightcrewscheduling.support.TestHelper.PILOT_SKILL;
import static org.acme.flightcrewscheduling.support.TestHelper.aFlight;
import static org.acme.flightcrewscheduling.support.TestHelper.aFlightAssignment;
import static org.acme.flightcrewscheduling.support.TestHelper.anAirport;
import static org.acme.flightcrewscheduling.support.TestHelper.anEmployee;
import static org.acme.flightcrewscheduling.support.TestHelper.offsetDateTime;

import java.util.List;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.flightcrewscheduling.domain.Airport;
import org.acme.flightcrewscheduling.domain.Employee;
import org.acme.flightcrewscheduling.domain.Flight;
import org.acme.flightcrewscheduling.domain.FlightAssignment;
import org.acme.flightcrewscheduling.domain.FlightCrewSchedule;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class FlightCrewSchedulingConstraintProviderTest {

    private static final Airport LHR = anAirport("LHR");
    private static final Airport JFK = anAirport("JFK");
    private static final Airport ATL = anAirport("ATL");

    @Inject
    ConstraintVerifier<FlightCrewSchedulingConstraintProvider, FlightCrewSchedule> constraintVerifier;

    @Test
    void requiredSkill() {
        Employee attendant = anEmployee("crew-1").skills(List.of(ATTENDANT_SKILL)).build();
        FlightAssignment pilotSeat = aFlightAssignment("seat-1", outboundFlight())
                .requiredSkill(PILOT_SKILL)
                .employee(attendant)
                .build();

        constraintVerifier.verifyThat(FlightCrewSchedulingConstraintProvider::requiredSkill)
                .given(pilotSeat)
                .penalizesBy(1); // the flight attendant cannot fly the plane
    }

    @Test
    void flightConflict() {
        Employee pilot = anEmployee("crew-1").build();
        FlightAssignment first = aFlightAssignment("seat-1", outboundFlight()).employee(pilot).build();
        Flight overlappingFlight = aFlight("TF9")
                .departure(LHR, offsetDateTime(0, 10))
                .arrival(ATL, offsetDateTime(0, 19))
                .build();
        FlightAssignment overlapping = aFlightAssignment("seat-2", overlappingFlight).employee(pilot).build();

        constraintVerifier.verifyThat(FlightCrewSchedulingConstraintProvider::flightConflict)
                .given(first, overlapping)
                .penalizesBy(1); // one overlapping pair
    }

    @Test
    void transferBetweenTwoFlights() {
        Employee pilot = anEmployee("crew-1").build();
        FlightAssignment first = aFlightAssignment("seat-1", outboundFlight()).employee(pilot).build();

        // Departs from LHR, but the crew member landed at JFK on the previous flight.
        Flight strandedFlight = aFlight("TF3")
                .departure(LHR, offsetDateTime(1, 8))
                .arrival(ATL, offsetDateTime(1, 17))
                .build();
        FlightAssignment stranded = aFlightAssignment("seat-2", strandedFlight).employee(pilot).build();

        constraintVerifier.verifyThat(FlightCrewSchedulingConstraintProvider::transferBetweenTwoFlights)
                .given(first, stranded)
                .penalizesBy(1); // one impossible transfer

        FlightAssignment connecting = aFlightAssignment("seat-3", inboundFlight()).employee(pilot).build();

        constraintVerifier.verifyThat(FlightCrewSchedulingConstraintProvider::transferBetweenTwoFlights)
                .given(first, connecting)
                .penalizesBy(0); // TF2 departs from JFK, where TF1 landed
    }

    @Test
    void employeeUnavailability() {
        Employee pilot = anEmployee("crew-1").unavailableDays(List.of(FIRST_DAY)).build();
        FlightAssignment onUnavailableDay = aFlightAssignment("seat-1", outboundFlight()).employee(pilot).build();

        constraintVerifier.verifyThat(FlightCrewSchedulingConstraintProvider::employeeUnavailability)
                .given(onUnavailableDay)
                .penalizesBy(1); // unavailable on the day of the flight

        FlightAssignment onAvailableDay = aFlightAssignment("seat-2", inboundFlight()).employee(pilot).build();

        constraintVerifier.verifyThat(FlightCrewSchedulingConstraintProvider::employeeUnavailability)
                .given(onAvailableDay)
                .penalizesBy(0); // the return leg is a day later
    }

    @Test
    void firstAssignmentNotDepartingFromHome() {
        Employee pilot = anEmployee("crew-1").homeAirport(LHR).build();
        // The earliest assignment departs from JFK instead of the crew member's home airport LHR.
        FlightAssignment first = aFlightAssignment("seat-1", inboundFlight()).employee(pilot).build();
        Flight laterFlight = aFlight("TF3")
                .departure(LHR, offsetDateTime(2, 6))
                .arrival(ATL, offsetDateTime(2, 15))
                .build();
        FlightAssignment later = aFlightAssignment("seat-2", laterFlight).employee(pilot).build();

        constraintVerifier.verifyThat(FlightCrewSchedulingConstraintProvider::firstAssignmentNotDepartingFromHome)
                .given(pilot, first, later)
                .penalizesBy(1); // only the earliest assignment is checked
    }

    @Test
    void lastAssignmentNotArrivingAtHome() {
        Employee pilot = anEmployee("crew-1").homeAirport(LHR).build();
        FlightAssignment first = aFlightAssignment("seat-1", outboundFlight()).employee(pilot).build();
        // The latest assignment lands at ATL instead of the crew member's home airport LHR.
        Flight strayFlight = aFlight("TF3")
                .departure(JFK, offsetDateTime(1, 8))
                .arrival(ATL, offsetDateTime(1, 14))
                .build();
        FlightAssignment last = aFlightAssignment("seat-2", strayFlight).employee(pilot).build();

        constraintVerifier.verifyThat(FlightCrewSchedulingConstraintProvider::lastAssignmentNotArrivingAtHome)
                .given(pilot, first, last)
                .penalizesBy(1); // only the latest assignment is checked
    }

    private static Flight outboundFlight() {
        return aFlight("TF1")
                .departure(LHR, offsetDateTime(0, 6))
                .arrival(JFK, offsetDateTime(0, 14))
                .build();
    }

    private static Flight inboundFlight() {
        return aFlight("TF2")
                .departure(JFK, offsetDateTime(1, 8))
                .arrival(LHR, offsetDateTime(1, 16))
                .build();
    }
}
