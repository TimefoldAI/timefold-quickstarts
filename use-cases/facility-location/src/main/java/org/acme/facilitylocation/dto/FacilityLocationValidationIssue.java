package org.acme.facilitylocation.dto;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Validation issues that can be found in a facility location problem.")
@SuppressWarnings("ImmutableEnumChecker")
public enum FacilityLocationValidationIssue {
    FACILITY_ID_MISSING(IssueCode.of("FACILITY_ID_MISSING"), IssueSeverity.ERROR,
            "Facility ID must not be null or blank."),
    DUPLICATE_FACILITY_ID(IssueCode.of("DUPLICATE_FACILITY_ID"), IssueSeverity.ERROR,
            "Duplicate facility ID found."),
    NEGATIVE_FACILITY_CAPACITY(IssueCode.of("NEGATIVE_FACILITY_CAPACITY"), IssueSeverity.ERROR,
            "Facility has negative capacity."),
    NEGATIVE_FACILITY_SETUP_COST(IssueCode.of("NEGATIVE_FACILITY_SETUP_COST"), IssueSeverity.ERROR,
            "Facility has negative setup cost."),
    CONSUMER_ID_MISSING(IssueCode.of("CONSUMER_ID_MISSING"), IssueSeverity.ERROR,
            "Consumer ID must not be null or blank."),
    DUPLICATE_CONSUMER_ID(IssueCode.of("DUPLICATE_CONSUMER_ID"), IssueSeverity.ERROR,
            "Duplicate consumer ID found."),
    NEGATIVE_CONSUMER_DEMAND(IssueCode.of("NEGATIVE_CONSUMER_DEMAND"), IssueSeverity.ERROR,
            "Consumer has negative demand."),
    NON_EXISTING_FACILITY_REFERENCE(IssueCode.of("NON_EXISTING_FACILITY_REFERENCE"), IssueSeverity.ERROR,
            "Consumer references non-existing facility.");

    private final transient IssueType issueType;

    FacilityLocationValidationIssue(IssueCode code, IssueSeverity severity, String message) {
        this.issueType = new IssueType(code, severity, message);
    }

    public IssueType asIssueType() {
        return issueType;
    }
}
