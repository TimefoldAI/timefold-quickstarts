package org.acme.flightcrewscheduling.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A dataset validation issue reported for a flight crew scheduling input.",
        oneOf = {
                FlightCrewScheduleIssue.DuplicateAirportCodeIssue.class,
                FlightCrewScheduleIssue.DuplicateEmployeeIdIssue.class,
                FlightCrewScheduleIssue.DuplicateFlightNumberIssue.class,
                FlightCrewScheduleIssue.DuplicateFlightAssignmentIdIssue.class,
                FlightCrewScheduleIssue.NonExistingAirportReferenceIssue.class,
                FlightCrewScheduleIssue.NonExistingFlightReferenceIssue.class,
                FlightCrewScheduleIssue.NonExistingEmployeeReferenceIssue.class,
                FlightCrewScheduleIssue.FlightArrivesBeforeItDepartsIssue.class
        })
public abstract class FlightCrewScheduleIssue extends AbstractIssue {

    protected FlightCrewScheduleIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
        super(code, severity, metadata);
    }

    @Schema(allOf = { FlightCrewScheduleIssue.class })
    public static class DuplicateAirportCodeIssue extends FlightCrewScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_AIRPORT_CODE");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate airport code found.");

        @Schema(description = "The code of the duplicated airport.")
        private String airportCode;

        public DuplicateAirportCodeIssue() {
            this(null);
        }

        public DuplicateAirportCodeIssue(String airportCode) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.airportCode = airportCode;
        }

        public String getAirportCode() {
            return airportCode;
        }
    }

    @Schema(allOf = { FlightCrewScheduleIssue.class })
    public static class DuplicateEmployeeIdIssue extends FlightCrewScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_EMPLOYEE_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate crew member ID found.");

        @Schema(description = "The ID of the duplicated crew member.")
        private String employeeId;

        public DuplicateEmployeeIdIssue() {
            this(null);
        }

        public DuplicateEmployeeIdIssue(String employeeId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.employeeId = employeeId;
        }

        public String getEmployeeId() {
            return employeeId;
        }
    }

    @Schema(allOf = { FlightCrewScheduleIssue.class })
    public static class DuplicateFlightNumberIssue extends FlightCrewScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_FLIGHT_NUMBER");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate flight number found.");

        @Schema(description = "The number of the duplicated flight.")
        private String flightNumber;

        public DuplicateFlightNumberIssue() {
            this(null);
        }

        public DuplicateFlightNumberIssue(String flightNumber) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.flightNumber = flightNumber;
        }

        public String getFlightNumber() {
            return flightNumber;
        }
    }

    @Schema(allOf = { FlightCrewScheduleIssue.class })
    public static class DuplicateFlightAssignmentIdIssue extends FlightCrewScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_FLIGHT_ASSIGNMENT_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate flight assignment ID found.");

        @Schema(description = "The ID of the duplicated flight assignment.")
        private String flightAssignmentId;

        public DuplicateFlightAssignmentIdIssue() {
            this(null);
        }

        public DuplicateFlightAssignmentIdIssue(String flightAssignmentId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.flightAssignmentId = flightAssignmentId;
        }

        public String getFlightAssignmentId() {
            return flightAssignmentId;
        }
    }

    @Schema(allOf = { FlightCrewScheduleIssue.class })
    public static class NonExistingAirportReferenceIssue extends FlightCrewScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_AIRPORT_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Reference to an airport that does not exist.");

        @Schema(description = "The airport code that no airport in the dataset has.")
        private String airportCode;

        public NonExistingAirportReferenceIssue() {
            this(null);
        }

        public NonExistingAirportReferenceIssue(String airportCode) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.airportCode = airportCode;
        }

        public String getAirportCode() {
            return airportCode;
        }
    }

    @Schema(allOf = { FlightCrewScheduleIssue.class })
    public static class NonExistingFlightReferenceIssue extends FlightCrewScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_FLIGHT_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Flight assignment refers to a flight that does not exist.");

        @Schema(description = "The ID of the flight assignment with the unknown flight reference.")
        private String flightAssignmentId;

        public NonExistingFlightReferenceIssue() {
            this(null);
        }

        public NonExistingFlightReferenceIssue(String flightAssignmentId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.flightAssignmentId = flightAssignmentId;
        }

        public String getFlightAssignmentId() {
            return flightAssignmentId;
        }
    }

    @Schema(allOf = { FlightCrewScheduleIssue.class })
    public static class NonExistingEmployeeReferenceIssue extends FlightCrewScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_EMPLOYEE_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Flight assignment refers to a crew member that does not exist.");

        @Schema(description = "The ID of the flight assignment with the unknown crew member reference.")
        private String flightAssignmentId;

        public NonExistingEmployeeReferenceIssue() {
            this(null);
        }

        public NonExistingEmployeeReferenceIssue(String flightAssignmentId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.flightAssignmentId = flightAssignmentId;
        }

        public String getFlightAssignmentId() {
            return flightAssignmentId;
        }
    }

    @Schema(allOf = { FlightCrewScheduleIssue.class })
    public static class FlightArrivesBeforeItDepartsIssue extends FlightCrewScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("FLIGHT_ARRIVES_BEFORE_IT_DEPARTS");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Flight arrives before, or at the same time as, it departs.");

        @Schema(description = "The number of the flight with the impossible schedule.")
        private String flightNumber;

        public FlightArrivesBeforeItDepartsIssue() {
            this(null);
        }

        public FlightArrivesBeforeItDepartsIssue(String flightNumber) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.flightNumber = flightNumber;
        }

        public String getFlightNumber() {
            return flightNumber;
        }
    }
}
