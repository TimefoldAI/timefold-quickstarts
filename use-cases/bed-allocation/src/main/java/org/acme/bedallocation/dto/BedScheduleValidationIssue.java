package org.acme.bedallocation.dto;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Validation issues that can be found in a bed allocation problem.")
@SuppressWarnings("ImmutableEnumChecker")
public enum BedScheduleValidationIssue {
    STAY_ID_MISSING(IssueCode.of("STAY_ID_MISSING"), IssueSeverity.ERROR,
            "Stay ID must not be null or blank."),
    DUPLICATE_STAY_ID(IssueCode.of("DUPLICATE_STAY_ID"), IssueSeverity.ERROR,
            "Duplicate stay ID found."),
    BED_ID_MISSING(IssueCode.of("BED_ID_MISSING"), IssueSeverity.ERROR,
            "Bed ID must not be null or blank."),
    DUPLICATE_BED_ID(IssueCode.of("DUPLICATE_BED_ID"), IssueSeverity.ERROR,
            "Duplicate bed ID found."),
    NON_EXISTING_BED_REFERENCE(IssueCode.of("NON_EXISTING_BED_REFERENCE"), IssueSeverity.ERROR,
            "Stay references non-existing bed.");

    private final transient IssueType issueType;

    BedScheduleValidationIssue(IssueCode code, IssueSeverity severity, String message) {
        this.issueType = new IssueType(code, severity, message);
    }

    public IssueType asIssueType() {
        return issueType;
    }
}
