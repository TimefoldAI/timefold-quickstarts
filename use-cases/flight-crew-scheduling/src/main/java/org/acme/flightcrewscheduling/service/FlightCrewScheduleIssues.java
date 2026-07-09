package org.acme.flightcrewscheduling.service;

import java.util.List;
import java.util.stream.Stream;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.acme.flightcrewscheduling.dto.AirportIdDetail;
import org.acme.flightcrewscheduling.dto.EmployeeIdDetail;
import org.acme.flightcrewscheduling.dto.FlightAssignmentIdDetail;
import org.acme.flightcrewscheduling.dto.FlightCrewScheduleValidationIssue;
import org.acme.flightcrewscheduling.dto.FlightIdDetail;

@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class FlightCrewScheduleIssues {

    private FlightCrewScheduleIssues() {
    }

    public abstract static class FlightCrewScheduleIssue extends AbstractIssue {
        protected FlightCrewScheduleIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
            super(code, severity, metadata);
        }
    }

    public static final class AirportIdMissingIssue extends FlightCrewScheduleIssue {
        private static final IssueType TYPE = FlightCrewScheduleValidationIssue.AIRPORT_ID_MISSING.asIssueType();

        public AirportIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateAirportIdIssue extends FlightCrewScheduleIssue {
        private static final IssueType TYPE = FlightCrewScheduleValidationIssue.DUPLICATE_AIRPORT_ID.asIssueType();

        public DuplicateAirportIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateAirportIdIssue(AirportIdDetail airportIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(airportIdDetail)).toList());
        }
    }

    public static final class EmployeeIdMissingIssue extends FlightCrewScheduleIssue {
        private static final IssueType TYPE = FlightCrewScheduleValidationIssue.EMPLOYEE_ID_MISSING.asIssueType();

        public EmployeeIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateEmployeeIdIssue extends FlightCrewScheduleIssue {
        private static final IssueType TYPE = FlightCrewScheduleValidationIssue.DUPLICATE_EMPLOYEE_ID.asIssueType();

        public DuplicateEmployeeIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateEmployeeIdIssue(EmployeeIdDetail employeeIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(employeeIdDetail)).toList());
        }
    }

    public static final class FlightNumberMissingIssue extends FlightCrewScheduleIssue {
        private static final IssueType TYPE = FlightCrewScheduleValidationIssue.FLIGHT_NUMBER_MISSING.asIssueType();

        public FlightNumberMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateFlightNumberIssue extends FlightCrewScheduleIssue {
        private static final IssueType TYPE = FlightCrewScheduleValidationIssue.DUPLICATE_FLIGHT_NUMBER.asIssueType();

        public DuplicateFlightNumberIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateFlightNumberIssue(FlightIdDetail flightIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(flightIdDetail)).toList());
        }
    }

    public static final class FlightAssignmentIdMissingIssue extends FlightCrewScheduleIssue {
        private static final IssueType TYPE =
                FlightCrewScheduleValidationIssue.FLIGHT_ASSIGNMENT_ID_MISSING.asIssueType();

        public FlightAssignmentIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateFlightAssignmentIdIssue extends FlightCrewScheduleIssue {
        private static final IssueType TYPE =
                FlightCrewScheduleValidationIssue.DUPLICATE_FLIGHT_ASSIGNMENT_ID.asIssueType();

        public DuplicateFlightAssignmentIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateFlightAssignmentIdIssue(FlightAssignmentIdDetail flightAssignmentIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(flightAssignmentIdDetail)).toList());
        }
    }

    public static final class NonExistingHomeAirportReferenceIssue extends FlightCrewScheduleIssue {
        private static final IssueType TYPE =
                FlightCrewScheduleValidationIssue.NON_EXISTING_HOME_AIRPORT_REFERENCE.asIssueType();

        public NonExistingHomeAirportReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingHomeAirportReferenceIssue(EmployeeIdDetail employeeIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(employeeIdDetail)).toList());
        }
    }

    public static final class NonExistingDepartureAirportReferenceIssue extends FlightCrewScheduleIssue {
        private static final IssueType TYPE =
                FlightCrewScheduleValidationIssue.NON_EXISTING_DEPARTURE_AIRPORT_REFERENCE.asIssueType();

        public NonExistingDepartureAirportReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingDepartureAirportReferenceIssue(FlightIdDetail flightIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(flightIdDetail)).toList());
        }
    }

    public static final class NonExistingArrivalAirportReferenceIssue extends FlightCrewScheduleIssue {
        private static final IssueType TYPE =
                FlightCrewScheduleValidationIssue.NON_EXISTING_ARRIVAL_AIRPORT_REFERENCE.asIssueType();

        public NonExistingArrivalAirportReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingArrivalAirportReferenceIssue(FlightIdDetail flightIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(flightIdDetail)).toList());
        }
    }

    public static final class NonExistingFlightReferenceIssue extends FlightCrewScheduleIssue {
        private static final IssueType TYPE =
                FlightCrewScheduleValidationIssue.NON_EXISTING_FLIGHT_REFERENCE.asIssueType();

        public NonExistingFlightReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingFlightReferenceIssue(FlightAssignmentIdDetail flightAssignmentIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(flightAssignmentIdDetail)).toList());
        }
    }

    public static final class NonExistingEmployeeReferenceIssue extends FlightCrewScheduleIssue {
        private static final IssueType TYPE =
                FlightCrewScheduleValidationIssue.NON_EXISTING_EMPLOYEE_REFERENCE.asIssueType();

        public NonExistingEmployeeReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingEmployeeReferenceIssue(FlightAssignmentIdDetail flightAssignmentIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(flightAssignmentIdDetail)).toList());
        }
    }
}
