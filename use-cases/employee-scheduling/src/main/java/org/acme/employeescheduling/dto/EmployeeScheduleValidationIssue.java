package org.acme.employeescheduling.dto;

import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Validation issues that can be found in an employee scheduling problem.")
@SuppressWarnings("ImmutableEnumChecker")
public enum EmployeeScheduleValidationIssue {
    EMPLOYEE_ID_MISSING(IssueCode.of("EMPLOYEE_ID_MISSING"), IssueSeverity.ERROR,
            "Employee ID must not be null or blank."),
    DUPLICATE_EMPLOYEE_ID(IssueCode.of("DUPLICATE_EMPLOYEE_ID"), IssueSeverity.ERROR,
            "Duplicate employee ID found."),
    SHIFT_ID_MISSING(IssueCode.of("SHIFT_ID_MISSING"), IssueSeverity.ERROR,
            "Shift ID must not be null or blank."),
    DUPLICATE_SHIFT_ID(IssueCode.of("DUPLICATE_SHIFT_ID"), IssueSeverity.ERROR,
            "Duplicate shift ID found."),
    NON_EXISTING_EMPLOYEE_REFERENCE(IssueCode.of("NON_EXISTING_EMPLOYEE_REFERENCE"), IssueSeverity.ERROR,
            "Shift references non-existing employee.");

    private final transient IssueType issueType;

    EmployeeScheduleValidationIssue(IssueCode code, IssueSeverity severity, String message) {
        this.issueType = new IssueType(code, severity, message);
    }

    public IssueType asIssueType() {
        return issueType;
    }
}
