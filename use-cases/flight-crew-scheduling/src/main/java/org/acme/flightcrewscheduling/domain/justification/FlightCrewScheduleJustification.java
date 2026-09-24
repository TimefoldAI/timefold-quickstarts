package org.acme.flightcrewscheduling.domain.justification;

import ai.timefold.solver.service.definition.api.ModelConstraintJustification;

import org.acme.flightcrewscheduling.domain.Employee;
import org.acme.flightcrewscheduling.domain.FlightAssignment;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Common contract for every flight crew scheduling justification.
 * <p>
 * Each implementation is a record dedicated to exactly one thing that is being justified, so that the Timefold Platform can
 * both render a human-readable {@link #getDescription() description} and expose the individual facts behind it through the
 * OpenAPI schema.
 * <p>
 * Every implementation must be listed in the {@link Schema#oneOf()} below, otherwise it does not show up in the generated
 * OpenAPI schema.
 */
@Schema(description = "Explains why a flight crew scheduling constraint was matched.",
        oneOf = {
                // Hard constraints
                FlightCrewScheduleJustification.MissingRequiredSkillJustification.class,
                FlightCrewScheduleJustification.FlightConflictJustification.class,
                FlightCrewScheduleJustification.ImpossibleTransferJustification.class,
                FlightCrewScheduleJustification.EmployeeUnavailableJustification.class,

                // Soft constraints
                FlightCrewScheduleJustification.FirstAssignmentNotDepartingFromHomeJustification.class,
                FlightCrewScheduleJustification.LastAssignmentNotArrivingAtHomeJustification.class
        })
public interface FlightCrewScheduleJustification extends ModelConstraintJustification {

    /**
     * @return never null, a human-readable explanation of the constraint match
     */
    String getDescription();

    /**
     * Exposes the description as the {@code description} property of {@link ModelConstraintJustification}.
     */
    default String description() {
        return getDescription();
    }

    @Schema(allOf = { FlightCrewScheduleJustification.class })
    record MissingRequiredSkillJustification(String flightAssignment, String flight, String employee, String requiredSkill)
            implements
                FlightCrewScheduleJustification {

        public static MissingRequiredSkillJustification of(FlightAssignment assignment) {
            return new MissingRequiredSkillJustification(assignment.getId(), assignment.getFlight().flightNumber(),
                    assignment.getEmployee().id(), assignment.getRequiredSkill());
        }

        @Override
        public String getDescription() {
            return "Crew member '%s' is assigned to flight '%s' (assignment '%s') without the required skill '%s'."
                    .formatted(employee, flight, flightAssignment, requiredSkill);
        }
    }

    @Schema(allOf = { FlightCrewScheduleJustification.class })
    record FlightConflictJustification(String employee, String flightAssignment1, String flight1,
            String flightAssignment2, String flight2) implements FlightCrewScheduleJustification {

        public static FlightConflictJustification of(FlightAssignment left, FlightAssignment right) {
            return new FlightConflictJustification(left.getEmployee().id(), left.getId(),
                    left.getFlight().flightNumber(), right.getId(), right.getFlight().flightNumber());
        }

        @Override
        public String getDescription() {
            return "Crew member '%s' is assigned to overlapping flights '%s' (assignment '%s') and '%s' (assignment '%s')."
                    .formatted(employee, flight1, flightAssignment1, flight2, flightAssignment2);
        }
    }

    @Schema(allOf = { FlightCrewScheduleJustification.class })
    record ImpossibleTransferJustification(String employee, String flight, String arrivalAirport, String nextFlight,
            String nextDepartureAirport) implements FlightCrewScheduleJustification {

        public static ImpossibleTransferJustification of(FlightAssignment assignment, FlightAssignment nextAssignment) {
            return new ImpossibleTransferJustification(assignment.getEmployee().id(),
                    assignment.getFlight().flightNumber(), assignment.getArrivalAirport().code(),
                    nextAssignment.getFlight().flightNumber(), nextAssignment.getDepartureAirport().code());
        }

        @Override
        public String getDescription() {
            return "Crew member '%s' lands at '%s' on flight '%s', but their next flight '%s' departs from '%s'."
                    .formatted(employee, arrivalAirport, flight, nextFlight, nextDepartureAirport);
        }
    }

    @Schema(allOf = { FlightCrewScheduleJustification.class })
    record EmployeeUnavailableJustification(String employee, String flightAssignment, String flight,
            String departureDate, String arrivalDate) implements FlightCrewScheduleJustification {

        public static EmployeeUnavailableJustification of(FlightAssignment assignment) {
            return new EmployeeUnavailableJustification(assignment.getEmployee().id(), assignment.getId(),
                    assignment.getFlight().flightNumber(), assignment.getFlight().departureUTCDate().toString(),
                    assignment.getFlight().arrivalUTCDate().toString());
        }

        @Override
        public String getDescription() {
            return "Crew member '%s' is assigned to flight '%s' (assignment '%s') from %s to %s, but is unavailable on at least one of those days."
                    .formatted(employee, flight, flightAssignment, departureDate, arrivalDate);
        }
    }

    @Schema(allOf = { FlightCrewScheduleJustification.class })
    record FirstAssignmentNotDepartingFromHomeJustification(String employee, String homeAirport, String flight,
            String departureAirport) implements FlightCrewScheduleJustification {

        public static FirstAssignmentNotDepartingFromHomeJustification of(Employee employee, FlightAssignment assignment) {
            return new FirstAssignmentNotDepartingFromHomeJustification(employee.id(), employee.homeAirport().code(),
                    assignment.getFlight().flightNumber(), assignment.getDepartureAirport().code());
        }

        @Override
        public String getDescription() {
            return "Crew member '%s' starts at '%s' on flight '%s' instead of their home airport '%s'."
                    .formatted(employee, departureAirport, flight, homeAirport);
        }
    }

    @Schema(allOf = { FlightCrewScheduleJustification.class })
    record LastAssignmentNotArrivingAtHomeJustification(String employee, String homeAirport, String flight,
            String arrivalAirport) implements FlightCrewScheduleJustification {

        public static LastAssignmentNotArrivingAtHomeJustification of(Employee employee, FlightAssignment assignment) {
            return new LastAssignmentNotArrivingAtHomeJustification(employee.id(), employee.homeAirport().code(),
                    assignment.getFlight().flightNumber(), assignment.getArrivalAirport().code());
        }

        @Override
        public String getDescription() {
            return "Crew member '%s' ends at '%s' on flight '%s' instead of their home airport '%s'."
                    .formatted(employee, arrivalAirport, flight, homeAirport);
        }
    }
}
