package org.acme.orderpicking.dto;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Validation issues that can be found in an order picking problem.")
@SuppressWarnings("ImmutableEnumChecker")
public enum OrderPickingValidationIssue {
    TROLLEY_ID_MISSING(IssueCode.of("TROLLEY_ID_MISSING"), IssueSeverity.ERROR,
            "Trolley ID must not be null or blank."),
    DUPLICATE_TROLLEY_ID(IssueCode.of("DUPLICATE_TROLLEY_ID"), IssueSeverity.ERROR,
            "Duplicate trolley ID found."),
    PICK_TASK_ID_MISSING(IssueCode.of("PICK_TASK_ID_MISSING"), IssueSeverity.ERROR,
            "Pick task ID must not be null or blank."),
    DUPLICATE_PICK_TASK_ID(IssueCode.of("DUPLICATE_PICK_TASK_ID"), IssueSeverity.ERROR,
            "Duplicate pick task ID found."),
    NON_EXISTING_PICK_TASK_REFERENCE(IssueCode.of("NON_EXISTING_PICK_TASK_REFERENCE"), IssueSeverity.ERROR,
            "Trolley references a non-existing pick task."),
    DUPLICATE_PICK_TASK_ASSIGNMENT(IssueCode.of("DUPLICATE_PICK_TASK_ASSIGNMENT"), IssueSeverity.ERROR,
            "Pick task is assigned to more than one trolley.");

    private final transient IssueType issueType;

    OrderPickingValidationIssue(IssueCode code, IssueSeverity severity, String message) {
        this.issueType = new IssueType(code, severity, message);
    }

    public IssueType asIssueType() {
        return issueType;
    }
}
