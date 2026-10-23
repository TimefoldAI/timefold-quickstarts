package org.acme.employeescheduling.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A dataset validation issue reported for an employee scheduling input.",
        oneOf = {
                EmployeeScheduleIssue.DuplicateEmployeeNameIssue.class,
                EmployeeScheduleIssue.DuplicateShiftIdIssue.class,
                EmployeeScheduleIssue.InvalidShiftTimeRangeIssue.class,
                EmployeeScheduleIssue.NonExistingEmployeeReferenceIssue.class,
                EmployeeScheduleIssue.NoQualifiedEmployeeIssue.class,
                EmployeeScheduleIssue.InsufficientEmployeesIssue.class
        })
public abstract class EmployeeScheduleIssue extends AbstractIssue {

    protected EmployeeScheduleIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
        super(code, severity, metadata);
    }

    @Schema(allOf = { EmployeeScheduleIssue.class })
    public static class DuplicateEmployeeNameIssue extends EmployeeScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_EMPLOYEE_NAME");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate employee name found.");

        @Schema(description = "The name of the duplicated employee.")
        private String employeeName;

        public DuplicateEmployeeNameIssue() {
            this(null);
        }

        public DuplicateEmployeeNameIssue(String employeeName) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.employeeName = employeeName;
        }

        public String getEmployeeName() {
            return employeeName;
        }
    }

    @Schema(allOf = { EmployeeScheduleIssue.class })
    public static class DuplicateShiftIdIssue extends EmployeeScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_SHIFT_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate shift ID found.");

        @Schema(description = "The ID of the duplicated shift.")
        private String shiftId;

        public DuplicateShiftIdIssue() {
            this(null);
        }

        public DuplicateShiftIdIssue(String shiftId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.shiftId = shiftId;
        }

        public String getShiftId() {
            return shiftId;
        }
    }

    /**
     * The constraints measure overlaps, rest time and availability from a shift's start to its end, which is
     * meaningless for a shift that ends before it starts.
     */
    @Schema(allOf = { EmployeeScheduleIssue.class })
    public static class InvalidShiftTimeRangeIssue extends EmployeeScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("INVALID_SHIFT_TIME_RANGE");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Shift does not end after it starts.");

        @Schema(description = "The ID of the shift whose end is not after its start.")
        private String shiftId;

        public InvalidShiftTimeRangeIssue() {
            this(null);
        }

        public InvalidShiftTimeRangeIssue(String shiftId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.shiftId = shiftId;
        }

        public String getShiftId() {
            return shiftId;
        }
    }

    @Schema(allOf = { EmployeeScheduleIssue.class })
    public static class NonExistingEmployeeReferenceIssue extends EmployeeScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_EMPLOYEE_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Shift refers to an employee name that does not exist.");

        @Schema(description = "The ID of the shift with the unknown employee reference.")
        private String shiftId;

        public NonExistingEmployeeReferenceIssue() {
            this(null);
        }

        public NonExistingEmployeeReferenceIssue(String shiftId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.shiftId = shiftId;
        }

        public String getShiftId() {
            return shiftId;
        }
    }

    /**
     * A shift can only be worked by an employee with its required skill, so a skill no employee has leaves every
     * shift that requires it without a feasible assignment. One issue per skill, not per shift.
     */
    @Schema(allOf = { EmployeeScheduleIssue.class })
    public static class NoQualifiedEmployeeIssue extends EmployeeScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NO_QUALIFIED_EMPLOYEE");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("No employee has the skill some shifts require.");

        @Schema(description = "The skill no employee has.")
        private String requiredSkill;
        @Schema(description = "The number of shifts that require the skill.")
        private int shiftCount;

        public NoQualifiedEmployeeIssue() {
            this(null, 0);
        }

        public NoQualifiedEmployeeIssue(String requiredSkill, int shiftCount) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.requiredSkill = requiredSkill;
            this.shiftCount = shiftCount;
        }

        public String getRequiredSkill() {
            return requiredSkill;
        }

        public int getShiftCount() {
            return shiftCount;
        }
    }

    /**
     * An employee works at most one shift a day, so a day with more shifts than employees can never be scheduled
     * without breaking that rule.
     */
    @Schema(allOf = { EmployeeScheduleIssue.class })
    public static class InsufficientEmployeesIssue extends EmployeeScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("INSUFFICIENT_EMPLOYEES");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("More shifts start on a day than there are employees to work them.");

        @Schema(description = "The day, in ISO-8601 date format.")
        private String date;
        @Schema(description = "The number of shifts that start on that day.")
        private int shiftCount;
        @Schema(description = "The number of employees in the dataset.")
        private int employeeCount;

        public InsufficientEmployeesIssue() {
            this(null, 0, 0);
        }

        public InsufficientEmployeesIssue(String date, int shiftCount, int employeeCount) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.date = date;
            this.shiftCount = shiftCount;
            this.employeeCount = employeeCount;
        }

        public String getDate() {
            return date;
        }

        public int getShiftCount() {
            return shiftCount;
        }

        public int getEmployeeCount() {
            return employeeCount;
        }
    }
}
