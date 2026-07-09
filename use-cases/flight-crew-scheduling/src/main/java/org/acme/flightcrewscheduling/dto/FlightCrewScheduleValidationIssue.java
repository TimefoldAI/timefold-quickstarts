package org.acme.flightcrewscheduling.dto;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Validation issues that can be found in a flight crew scheduling problem.")
@SuppressWarnings("ImmutableEnumChecker")
public enum FlightCrewScheduleValidationIssue {
    AIRPORT_ID_MISSING(IssueCode.of("AIRPORT_ID_MISSING"), IssueSeverity.ERROR,
            "Airport ID must not be null or blank."),
    DUPLICATE_AIRPORT_ID(IssueCode.of("DUPLICATE_AIRPORT_ID"), IssueSeverity.ERROR,
            "Duplicate airport ID found."),
    EMPLOYEE_ID_MISSING(IssueCode.of("EMPLOYEE_ID_MISSING"), IssueSeverity.ERROR,
            "Employee ID must not be null or blank."),
    DUPLICATE_EMPLOYEE_ID(IssueCode.of("DUPLICATE_EMPLOYEE_ID"), IssueSeverity.ERROR,
            "Duplicate employee ID found."),
    FLIGHT_NUMBER_MISSING(IssueCode.of("FLIGHT_NUMBER_MISSING"), IssueSeverity.ERROR,
            "Flight number must not be null or blank."),
    DUPLICATE_FLIGHT_NUMBER(IssueCode.of("DUPLICATE_FLIGHT_NUMBER"), IssueSeverity.ERROR,
            "Duplicate flight number found."),
    FLIGHT_ASSIGNMENT_ID_MISSING(IssueCode.of("FLIGHT_ASSIGNMENT_ID_MISSING"), IssueSeverity.ERROR,
            "Flight assignment ID must not be null or blank."),
    DUPLICATE_FLIGHT_ASSIGNMENT_ID(IssueCode.of("DUPLICATE_FLIGHT_ASSIGNMENT_ID"), IssueSeverity.ERROR,
            "Duplicate flight assignment ID found."),
    NON_EXISTING_HOME_AIRPORT_REFERENCE(IssueCode.of("NON_EXISTING_HOME_AIRPORT_REFERENCE"), IssueSeverity.ERROR,
            "Employee references non-existing home airport."),
    NON_EXISTING_DEPARTURE_AIRPORT_REFERENCE(IssueCode.of("NON_EXISTING_DEPARTURE_AIRPORT_REFERENCE"),
            IssueSeverity.ERROR, "Flight references non-existing departure airport."),
    NON_EXISTING_ARRIVAL_AIRPORT_REFERENCE(IssueCode.of("NON_EXISTING_ARRIVAL_AIRPORT_REFERENCE"),
            IssueSeverity.ERROR, "Flight references non-existing arrival airport."),
    NON_EXISTING_FLIGHT_REFERENCE(IssueCode.of("NON_EXISTING_FLIGHT_REFERENCE"), IssueSeverity.ERROR,
            "Flight assignment references non-existing flight."),
    NON_EXISTING_EMPLOYEE_REFERENCE(IssueCode.of("NON_EXISTING_EMPLOYEE_REFERENCE"), IssueSeverity.ERROR,
            "Flight assignment references non-existing employee.");

    private final transient IssueType issueType;

    FlightCrewScheduleValidationIssue(IssueCode code, IssueSeverity severity, String message) {
        this.issueType = new IssueType(code, severity, message);
    }

    public IssueType asIssueType() {
        return issueType;
    }
}
