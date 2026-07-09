package org.acme.foodpackaging.dto;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Validation issues that can be found in a food packaging problem.")
@SuppressWarnings("ImmutableEnumChecker")
public enum PackagingScheduleValidationIssue {
    JOB_ID_MISSING(IssueCode.of("JOB_ID_MISSING"), IssueSeverity.ERROR,
            "Job ID must not be null or blank."),
    DUPLICATE_JOB_ID(IssueCode.of("DUPLICATE_JOB_ID"), IssueSeverity.ERROR,
            "Duplicate job ID found."),
    LINE_ID_MISSING(IssueCode.of("LINE_ID_MISSING"), IssueSeverity.ERROR,
            "Line ID must not be null or blank."),
    DUPLICATE_LINE_ID(IssueCode.of("DUPLICATE_LINE_ID"), IssueSeverity.ERROR,
            "Duplicate line ID found."),
    NON_EXISTING_LINE_REFERENCE(IssueCode.of("NON_EXISTING_LINE_REFERENCE"), IssueSeverity.ERROR,
            "Job references non-existing line."),
    NON_EXISTING_PRODUCT_REFERENCE(IssueCode.of("NON_EXISTING_PRODUCT_REFERENCE"), IssueSeverity.ERROR,
            "Job references non-existing product.");

    private final transient IssueType issueType;

    PackagingScheduleValidationIssue(IssueCode code, IssueSeverity severity, String message) {
        this.issueType = new IssueType(code, severity, message);
    }

    public IssueType asIssueType() {
        return issueType;
    }
}
