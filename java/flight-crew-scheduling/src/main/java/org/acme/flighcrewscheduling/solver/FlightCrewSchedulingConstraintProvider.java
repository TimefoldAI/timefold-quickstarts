package org.acme.flighcrewscheduling.solver;

import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.filtering;
import static ai.timefold.solver.core.api.score.stream.Joiners.greaterThan;
import static ai.timefold.solver.core.api.score.stream.Joiners.lessThan;
import static ai.timefold.solver.core.api.score.stream.Joiners.overlapping;

import java.util.function.Function;

import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

import org.acme.flighcrewscheduling.domain.Employee;
import org.acme.flighcrewscheduling.domain.FlightAssignment;

public class FlightCrewSchedulingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                requiredSkill(constraintFactory),
                flightConflict(constraintFactory),
                transferBetweenTwoFlights(constraintFactory),
                employeeUnavailability(constraintFactory),

                // Soft constraints
                firstAssignmentNotDepartingFromHome(constraintFactory),
                lastAssignmentNotArrivingAtHome(constraintFactory)
        };
    }

    public Constraint requiredSkill(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(FlightAssignment.class)
                .filter(flightAssignment -> !flightAssignment.hasRequiredSkills())
                .penalize(HardSoftScore.ofHard(100))
                .asConstraint("Required skill");
    }

    public Constraint flightConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(FlightAssignment.class,
                equal(FlightAssignment::getEmployee),
                overlapping(flightAssignment -> flightAssignment.getFlight().getDepartureUTCDateTime(),
                        flightAssignment -> flightAssignment.getFlight().getArrivalUTCDateTime()))
                .penalize(HardSoftScore.ofHard(10))
                .asConstraint("Flight conflict");
    }

    public Constraint transferBetweenTwoFlights(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(FlightAssignment.class)
                .join(FlightAssignment.class, equal(FlightAssignment::getEmployee),
                        lessThan(FlightAssignment::getDepartureUTCDateTime),
                        filtering((flightAssignment,
                                flightAssignment2) -> !flightAssignment.getId().equals(flightAssignment2.getId())))
                .ifNotExists(FlightAssignment.class,
                        equal(((flightAssignment, flightAssignment2) -> flightAssignment.getEmployee()),
                                FlightAssignment::getEmployee),
                        filtering((flightAssignment, flightAssignment2,
                                otherFlightAssignment) -> !otherFlightAssignment.getId().equals(flightAssignment.getId())
                                        && !otherFlightAssignment.getId().equals(flightAssignment2.getId())
                                        && !otherFlightAssignment.getDepartureUTCDateTime()
                                                .isBefore(flightAssignment.getDepartureUTCDateTime())
                                        && otherFlightAssignment.getDepartureUTCDateTime()
                                                .isBefore(flightAssignment2.getDepartureUTCDateTime())))
                .filter((flightAssignment,
                        flightAssignment2) -> !flightAssignment.getFlight().getArrivalAirport()
                                .equals(flightAssignment2.getFlight().getDepartureAirport()))
                .penalize(HardSoftScore.ofHard(1))
                .asConstraint("Transfer between two flights");
    }

    public Constraint employeeUnavailability(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(FlightAssignment.class)
                .filter(FlightAssignment::isUnavailableEmployee)
                .penalize(HardSoftScore.ofHard(10))
                .asConstraint("Employee unavailable");
    }

    public Constraint firstAssignmentNotDepartingFromHome(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Employee.class)
                .join(FlightAssignment.class, equal(Function.identity(), FlightAssignment::getEmployee))
                .ifNotExists(FlightAssignment.class,
                        equal((employee, flightAssignment) -> employee, FlightAssignment::getEmployee),
                        greaterThan((employee, flightAssignment) -> flightAssignment.getDepartureUTCDateTime(),
                                FlightAssignment::getDepartureUTCDateTime))
                .filter((employee,
                        flightAssignment) -> !employee.getHomeAirport()
                                .equals(flightAssignment.getFlight().getDepartureAirport()))
                .penalize(HardSoftScore.ofSoft(1000))
                .asConstraint("First assignment not departing from home");
    }

    public Constraint lastAssignmentNotArrivingAtHome(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Employee.class)
                .join(FlightAssignment.class, equal(Function.identity(), FlightAssignment::getEmployee))
                .ifNotExists(FlightAssignment.class,
                        equal((employee, flightAssignment) -> employee, FlightAssignment::getEmployee),
                        lessThan((employee, flightAssignment) -> flightAssignment.getDepartureUTCDateTime(),
                                FlightAssignment::getDepartureUTCDateTime))
                .filter((employee,
                        flightAssignment) -> !employee.getHomeAirport()
                                .equals(flightAssignment.getFlight().getArrivalAirport()))
                .penalize(HardSoftScore.ofSoft(1000))
                .asConstraint("Last assignment not arriving at home");
    }

}
