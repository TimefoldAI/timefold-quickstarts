package org.acme.maintenancescheduling.dto;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Validation issues that can be found in a maintenance scheduling problem.")
@SuppressWarnings("ImmutableEnumChecker")
public enum MaintenanceScheduleValidationIssue {
    JOB_ID_MISSING(IssueCode.of("JOB_ID_MISSING"), IssueSeverity.ERROR,
            "Job ID must not be null or blank."),
    DUPLICATE_JOB_ID(IssueCode.of("DUPLICATE_JOB_ID"), IssueSeverity.ERROR,
            "Duplicate job ID found."),
    CREW_ID_MISSING(IssueCode.of("CREW_ID_MISSING"), IssueSeverity.ERROR,
            "Crew ID must not be null or blank."),
    DUPLICATE_CREW_ID(IssueCode.of("DUPLICATE_CREW_ID"), IssueSeverity.ERROR,
            "Duplicate crew ID found."),
    WORK_CALENDAR_MISSING(IssueCode.of("WORK_CALENDAR_MISSING"), IssueSeverity.ERROR,
            "Work calendar must be provided."),
    NON_EXISTING_CREW_REFERENCE(IssueCode.of("NON_EXISTING_CREW_REFERENCE"), IssueSeverity.ERROR,
            "Job references non-existing crew.");

    private final transient IssueType issueType;

    MaintenanceScheduleValidationIssue(IssueCode code, IssueSeverity severity, String message) {
        this.issueType = new IssueType(code, severity, message);
    }

    public IssueType asIssueType() {
        return issueType;
    }
}
