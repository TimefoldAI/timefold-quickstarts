package org.acme.projectjobschedule.dto;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Validation issues that can be found in a project job scheduling problem.")
@SuppressWarnings("ImmutableEnumChecker")
public enum ProjectJobScheduleValidationIssue {
    JOB_ID_MISSING(IssueCode.of("JOB_ID_MISSING"), IssueSeverity.ERROR,
            "Job ID must not be null or blank."),
    DUPLICATE_JOB_ID(IssueCode.of("DUPLICATE_JOB_ID"), IssueSeverity.ERROR,
            "Duplicate job ID found."),
    RESOURCE_ID_MISSING(IssueCode.of("RESOURCE_ID_MISSING"), IssueSeverity.ERROR,
            "Resource ID must not be null or blank."),
    DUPLICATE_RESOURCE_ID(IssueCode.of("DUPLICATE_RESOURCE_ID"), IssueSeverity.ERROR,
            "Duplicate resource ID found."),
    ALLOCATION_ID_MISSING(IssueCode.of("ALLOCATION_ID_MISSING"), IssueSeverity.ERROR,
            "Allocation ID must not be null or blank."),
    DUPLICATE_ALLOCATION_ID(IssueCode.of("DUPLICATE_ALLOCATION_ID"), IssueSeverity.ERROR,
            "Duplicate allocation ID found."),
    NON_EXISTING_JOB_REFERENCE(IssueCode.of("NON_EXISTING_JOB_REFERENCE"), IssueSeverity.ERROR,
            "Allocation references non-existing job."),
    NON_EXISTING_EXECUTION_MODE_REFERENCE(IssueCode.of("NON_EXISTING_EXECUTION_MODE_REFERENCE"), IssueSeverity.ERROR,
            "Allocation references non-existing execution mode.");

    private final transient IssueType issueType;

    ProjectJobScheduleValidationIssue(IssueCode code, IssueSeverity severity, String message) {
        this.issueType = new IssueType(code, severity, message);
    }

    public IssueType asIssueType() {
        return issueType;
    }
}
