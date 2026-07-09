package org.acme.flightcrewscheduling.solver;

import java.util.function.Function;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import ai.timefold.solver.service.definition.api.description.ConstraintInfo;

import org.acme.flightcrewscheduling.domain.Employee;
import org.acme.flightcrewscheduling.domain.FlightAssignment;

public class FlightCrewSchedulingConstraintProvider implements ConstraintProvider {

    public static final String REQUIRED_SKILL = "Required skill";
    public static final String FLIGHT_CONFLICT = "Flight conflict";
    public static final String TRANSFER_BETWEEN_TWO_FLIGHTS = "Transfer between two flights";
    public static final String EMPLOYEE_UNAVAILABLE = "Employee unavailable";
    public static final String FIRST_ASSIGNMENT_NOT_DEPARTING_FROM_HOME = "First assignment not departing from home";
    public static final String LAST_ASSIGNMENT_NOT_ARRIVING_AT_HOME = "Last assignment not arriving at home";

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
                .penalize(HardMediumSoftScore.ofHard(100))
                .asConstraint(new ConstraintInfo(REQUIRED_SKILL, REQUIRED_SKILL,
                        "An employee must have the skill required by the flight assignment.",
                        FlightCrewSchedulingConstraintGroup.CREW_FEASIBILITY));
    }

    public Constraint flightConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(FlightAssignment.class,
                Joiners.equal(FlightAssignment::getEmployee),
                Joiners.overlapping(flightAssignment -> flightAssignment.getFlight().getDepartureUTCDateTime(),
                        flightAssignment -> flightAssignment.getFlight().getArrivalUTCDateTime()))
                .penalize(HardMediumSoftScore.ofHard(10))
                .asConstraint(new ConstraintInfo(FLIGHT_CONFLICT, FLIGHT_CONFLICT,
                        "An employee cannot be assigned to two overlapping flights at the same time.",
                        FlightCrewSchedulingConstraintGroup.CREW_FEASIBILITY));
    }

    public Constraint transferBetweenTwoFlights(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(FlightAssignment.class)
                .join(FlightAssignment.class, Joiners.equal(FlightAssignment::getEmployee),
                        Joiners.lessThan(FlightAssignment::getDepartureUTCDateTime),
                        Joiners.filtering((flightAssignment,
                                flightAssignment2) -> !flightAssignment.getId().equals(flightAssignment2.getId())))
                .ifNotExists(FlightAssignment.class,
                        Joiners.equal((flightAssignment, flightAssignment2) -> flightAssignment.getEmployee(),
                                FlightAssignment::getEmployee),
                        Joiners.filtering((flightAssignment, flightAssignment2,
                                otherFlightAssignment) -> !otherFlightAssignment.getId().equals(flightAssignment.getId())
                                        && !otherFlightAssignment.getId().equals(flightAssignment2.getId())
                                        && !otherFlightAssignment.getDepartureUTCDateTime()
                                                .isBefore(flightAssignment.getDepartureUTCDateTime())
                                        && otherFlightAssignment.getDepartureUTCDateTime()
                                                .isBefore(flightAssignment2.getDepartureUTCDateTime())))
                .filter((flightAssignment,
                        flightAssignment2) -> !flightAssignment.getFlight().getArrivalAirport()
                                .equals(flightAssignment2.getFlight().getDepartureAirport()))
                .penalize(HardMediumSoftScore.ofHard(1))
                .asConstraint(new ConstraintInfo(TRANSFER_BETWEEN_TWO_FLIGHTS, TRANSFER_BETWEEN_TWO_FLIGHTS,
                        "Two consecutive flights of an employee must connect at the same airport.",
                        FlightCrewSchedulingConstraintGroup.CREW_FEASIBILITY));
    }

    public Constraint employeeUnavailability(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(FlightAssignment.class)
                .filter(FlightAssignment::isUnavailableEmployee)
                .penalize(HardMediumSoftScore.ofHard(10))
                .asConstraint(new ConstraintInfo(EMPLOYEE_UNAVAILABLE, EMPLOYEE_UNAVAILABLE,
                        "An employee must not be assigned to a flight on a day they are unavailable.",
                        FlightCrewSchedulingConstraintGroup.CREW_FEASIBILITY));
    }

    public Constraint firstAssignmentNotDepartingFromHome(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Employee.class)
                .join(FlightAssignment.class, Joiners.equal(Function.identity(), FlightAssignment::getEmployee))
                .ifNotExists(FlightAssignment.class,
                        Joiners.equal((employee, flightAssignment) -> employee, FlightAssignment::getEmployee),
                        Joiners.greaterThan((employee, flightAssignment) -> flightAssignment.getDepartureUTCDateTime(),
                                FlightAssignment::getDepartureUTCDateTime))
                .filter((employee,
                        flightAssignment) -> !employee.getHomeAirport()
                                .equals(flightAssignment.getFlight().getDepartureAirport()))
                .penalize(HardMediumSoftScore.ofSoft(1000))
                .asConstraint(new ConstraintInfo(FIRST_ASSIGNMENT_NOT_DEPARTING_FROM_HOME,
                        FIRST_ASSIGNMENT_NOT_DEPARTING_FROM_HOME,
                        "An employee's first flight of the schedule should depart from their home airport.",
                        FlightCrewSchedulingConstraintGroup.HOME_BASE_PREFERENCES));
    }

    public Constraint lastAssignmentNotArrivingAtHome(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Employee.class)
                .join(FlightAssignment.class, Joiners.equal(Function.identity(), FlightAssignment::getEmployee))
                .ifNotExists(FlightAssignment.class,
                        Joiners.equal((employee, flightAssignment) -> employee, FlightAssignment::getEmployee),
                        Joiners.lessThan((employee, flightAssignment) -> flightAssignment.getDepartureUTCDateTime(),
                                FlightAssignment::getDepartureUTCDateTime))
                .filter((employee,
                        flightAssignment) -> !employee.getHomeAirport()
                                .equals(flightAssignment.getFlight().getArrivalAirport()))
                .penalize(HardMediumSoftScore.ofSoft(1000))
                .asConstraint(new ConstraintInfo(LAST_ASSIGNMENT_NOT_ARRIVING_AT_HOME,
                        LAST_ASSIGNMENT_NOT_ARRIVING_AT_HOME,
                        "An employee's last flight of the schedule should arrive at their home airport.",
                        FlightCrewSchedulingConstraintGroup.HOME_BASE_PREFERENCES));
    }

}
