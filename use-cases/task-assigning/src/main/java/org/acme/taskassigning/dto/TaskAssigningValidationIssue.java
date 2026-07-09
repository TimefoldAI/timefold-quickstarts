package org.acme.taskassigning.dto;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Validation issues that can be found in a task assigning problem.")
@SuppressWarnings("ImmutableEnumChecker")
public enum TaskAssigningValidationIssue {
    TASK_ID_MISSING(IssueCode.of("TASK_ID_MISSING"), IssueSeverity.ERROR,
            "Task ID must not be null or blank."),
    DUPLICATE_TASK_ID(IssueCode.of("DUPLICATE_TASK_ID"), IssueSeverity.ERROR,
            "Duplicate task ID found."),
    EMPLOYEE_ID_MISSING(IssueCode.of("EMPLOYEE_ID_MISSING"), IssueSeverity.ERROR,
            "Employee ID must not be null or blank."),
    DUPLICATE_EMPLOYEE_ID(IssueCode.of("DUPLICATE_EMPLOYEE_ID"), IssueSeverity.ERROR,
            "Duplicate employee ID found."),
    NON_EXISTING_TASK_REFERENCE(IssueCode.of("NON_EXISTING_TASK_REFERENCE"), IssueSeverity.ERROR,
            "Employee references a non-existing task."),
    DUPLICATE_TASK_ASSIGNMENT(IssueCode.of("DUPLICATE_TASK_ASSIGNMENT"), IssueSeverity.ERROR,
            "Task is assigned to more than one employee.");

    private final transient IssueType issueType;

    TaskAssigningValidationIssue(IssueCode code, IssueSeverity severity, String message) {
        this.issueType = new IssueType(code, severity, message);
    }

    public IssueType asIssueType() {
        return issueType;
    }
}
