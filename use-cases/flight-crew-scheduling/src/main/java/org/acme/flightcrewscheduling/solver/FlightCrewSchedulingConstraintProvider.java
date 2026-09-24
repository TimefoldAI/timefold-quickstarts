package org.acme.flightcrewscheduling.solver;

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
import ai.timefold.solver.service.definition.api.description.ConstraintInfo;

import org.acme.flightcrewscheduling.domain.Employee;
import org.acme.flightcrewscheduling.domain.FlightAssignment;
import org.acme.flightcrewscheduling.domain.FlightCrewScheduleConstraintProperties;
import org.acme.flightcrewscheduling.domain.justification.FlightCrewScheduleJustification.EmployeeUnavailableJustification;
import org.acme.flightcrewscheduling.domain.justification.FlightCrewScheduleJustification.FirstAssignmentNotDepartingFromHomeJustification;
import org.acme.flightcrewscheduling.domain.justification.FlightCrewScheduleJustification.FlightConflictJustification;
import org.acme.flightcrewscheduling.domain.justification.FlightCrewScheduleJustification.ImpossibleTransferJustification;
import org.acme.flightcrewscheduling.domain.justification.FlightCrewScheduleJustification.LastAssignmentNotArrivingAtHomeJustification;
import org.acme.flightcrewscheduling.domain.justification.FlightCrewScheduleJustification.MissingRequiredSkillJustification;

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
                .filter(flightAssignment -> !flightAssignment.hasRequiredSkill())
                .penalize(HardSoftScore.ofHard(100))
                .justifyWith((flightAssignment, score) -> MissingRequiredSkillJustification.of(flightAssignment))
                .asConstraint(new ConstraintInfo(FlightCrewScheduleConstraintProperties.REQUIRED_SKILL,
                        FlightCrewScheduleConstraintProperties.REQUIRED_SKILL,
                        "A crew member must hold the skill required by the seat they are assigned to.",
                        FlightCrewScheduleConstraintGroup.CREW_QUALIFICATION));
    }

    public Constraint flightConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(FlightAssignment.class,
                equal(FlightAssignment::getEmployee),
                overlapping(FlightAssignment::getDepartureUTCDateTime, FlightAssignment::getArrivalUTCDateTime))
                .penalize(HardSoftScore.ofHard(10))
                .justifyWith((left, right, score) -> FlightConflictJustification.of(left, right))
                .asConstraint(new ConstraintInfo(FlightCrewScheduleConstraintProperties.FLIGHT_CONFLICT,
                        FlightCrewScheduleConstraintProperties.FLIGHT_CONFLICT,
                        "A crew member must not be assigned to two flights whose times overlap.",
                        FlightCrewScheduleConstraintGroup.CREW_AVAILABILITY));
    }

    /**
     * Penalizes every pair of consecutive assignments of the same crew member (no other assignment of theirs departs in
     * between) where the crew member would have to be in two places at once: the earlier flight lands somewhere other
     * than where the later flight departs from.
     */
    public Constraint transferBetweenTwoFlights(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(FlightAssignment.class)
                .join(FlightAssignment.class, equal(FlightAssignment::getEmployee),
                        lessThan(FlightAssignment::getDepartureUTCDateTime),
                        filtering((flightAssignment, nextFlightAssignment) -> !flightAssignment.getId()
                                .equals(nextFlightAssignment.getId())))
                .ifNotExists(FlightAssignment.class,
                        equal((flightAssignment, nextFlightAssignment) -> flightAssignment.getEmployee(),
                                FlightAssignment::getEmployee),
                        filtering((flightAssignment, nextFlightAssignment,
                                otherFlightAssignment) -> !otherFlightAssignment.getId().equals(flightAssignment.getId())
                                        && !otherFlightAssignment.getId().equals(nextFlightAssignment.getId())
                                        && !otherFlightAssignment.getDepartureUTCDateTime()
                                                .isBefore(flightAssignment.getDepartureUTCDateTime())
                                        && otherFlightAssignment.getDepartureUTCDateTime()
                                                .isBefore(nextFlightAssignment.getDepartureUTCDateTime())))
                .filter((flightAssignment, nextFlightAssignment) -> !flightAssignment.getArrivalAirport()
                        .equals(nextFlightAssignment.getDepartureAirport()))
                .penalize(HardSoftScore.ofHard(1))
                .justifyWith((flightAssignment, nextFlightAssignment,
                        score) -> ImpossibleTransferJustification.of(flightAssignment, nextFlightAssignment))
                .asConstraint(new ConstraintInfo(FlightCrewScheduleConstraintProperties.TRANSFER_BETWEEN_TWO_FLIGHTS,
                        FlightCrewScheduleConstraintProperties.TRANSFER_BETWEEN_TWO_FLIGHTS,
                        "A crew member's next flight must depart from the airport where their previous flight landed.",
                        FlightCrewScheduleConstraintGroup.ROUTE_CONTINUITY));
    }

    public Constraint employeeUnavailability(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(FlightAssignment.class)
                .filter(FlightAssignment::isUnavailableEmployee)
                .penalize(HardSoftScore.ofHard(10))
                .justifyWith((flightAssignment, score) -> EmployeeUnavailableJustification.of(flightAssignment))
                .asConstraint(new ConstraintInfo(FlightCrewScheduleConstraintProperties.EMPLOYEE_UNAVAILABILITY,
                        FlightCrewScheduleConstraintProperties.EMPLOYEE_UNAVAILABILITY,
                        "A crew member must not be assigned to a flight that runs on a day they are unavailable.",
                        FlightCrewScheduleConstraintGroup.CREW_AVAILABILITY));
    }

    public Constraint firstAssignmentNotDepartingFromHome(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Employee.class)
                .join(FlightAssignment.class, equal(Function.identity(), FlightAssignment::getEmployee))
                .ifNotExists(FlightAssignment.class,
                        equal((employee, flightAssignment) -> employee, FlightAssignment::getEmployee),
                        greaterThan((employee, flightAssignment) -> flightAssignment.getDepartureUTCDateTime(),
                                FlightAssignment::getDepartureUTCDateTime))
                .filter((employee, flightAssignment) -> !employee.homeAirport()
                        .equals(flightAssignment.getDepartureAirport()))
                .penalize(HardSoftScore.ofSoft(1000))
                .justifyWith((employee, flightAssignment,
                        score) -> FirstAssignmentNotDepartingFromHomeJustification.of(employee, flightAssignment))
                .asConstraint(
                        new ConstraintInfo(
                                FlightCrewScheduleConstraintProperties.FIRST_ASSIGNMENT_NOT_DEPARTING_FROM_HOME,
                                FlightCrewScheduleConstraintProperties.FIRST_ASSIGNMENT_NOT_DEPARTING_FROM_HOME,
                                "A crew member's first flight of the roster should depart from their home airport.",
                                FlightCrewScheduleConstraintGroup.HOME_BASE));
    }

    public Constraint lastAssignmentNotArrivingAtHome(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Employee.class)
                .join(FlightAssignment.class, equal(Function.identity(), FlightAssignment::getEmployee))
                .ifNotExists(FlightAssignment.class,
                        equal((employee, flightAssignment) -> employee, FlightAssignment::getEmployee),
                        lessThan((employee, flightAssignment) -> flightAssignment.getDepartureUTCDateTime(),
                                FlightAssignment::getDepartureUTCDateTime))
                .filter((employee, flightAssignment) -> !employee.homeAirport()
                        .equals(flightAssignment.getArrivalAirport()))
                .penalize(HardSoftScore.ofSoft(1000))
                .justifyWith((employee, flightAssignment,
                        score) -> LastAssignmentNotArrivingAtHomeJustification.of(employee, flightAssignment))
                .asConstraint(new ConstraintInfo(
                        FlightCrewScheduleConstraintProperties.LAST_ASSIGNMENT_NOT_ARRIVING_AT_HOME,
                        FlightCrewScheduleConstraintProperties.LAST_ASSIGNMENT_NOT_ARRIVING_AT_HOME,
                        "A crew member's last flight of the roster should arrive at their home airport.",
                        FlightCrewScheduleConstraintGroup.HOME_BASE));
    }
}
