package org.acme.taskassigning.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A dataset validation issue reported for a task assigning input.",
        oneOf = {
                TaskAssigningIssue.DuplicateEmployeeIdIssue.class,
                TaskAssigningIssue.DuplicateTaskIdIssue.class,
                TaskAssigningIssue.DuplicateTaskTypeCodeIssue.class,
                TaskAssigningIssue.DuplicateCustomerIdIssue.class,
                TaskAssigningIssue.NonExistingTaskTypeReferenceIssue.class,
                TaskAssigningIssue.NonExistingCustomerReferenceIssue.class,
                TaskAssigningIssue.NonExistingCustomerAffinityReferenceIssue.class,
                TaskAssigningIssue.NonExistingTaskReferenceInAssignmentIssue.class,
                TaskAssigningIssue.TaskAssignedToMultipleEmployeesIssue.class
        })
public abstract class TaskAssigningIssue extends AbstractIssue {

    protected TaskAssigningIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
        super(code, severity, metadata);
    }

    @Schema(allOf = { TaskAssigningIssue.class })
    public static class DuplicateEmployeeIdIssue extends TaskAssigningIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_EMPLOYEE_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate employee ID found.");

        @Schema(description = "The ID of the duplicated employee.")
        private String employeeId;

        public DuplicateEmployeeIdIssue() {
            this(null);
        }

        public DuplicateEmployeeIdIssue(String employeeId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.employeeId = employeeId;
        }

        public String getEmployeeId() {
            return employeeId;
        }
    }

    @Schema(allOf = { TaskAssigningIssue.class })
    public static class DuplicateTaskIdIssue extends TaskAssigningIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_TASK_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate task ID found.");

        @Schema(description = "The ID of the duplicated task.")
        private String taskId;

        public DuplicateTaskIdIssue() {
            this(null);
        }

        public DuplicateTaskIdIssue(String taskId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.taskId = taskId;
        }

        public String getTaskId() {
            return taskId;
        }
    }

    @Schema(allOf = { TaskAssigningIssue.class })
    public static class DuplicateTaskTypeCodeIssue extends TaskAssigningIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_TASK_TYPE_CODE");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate task type code found.");

        @Schema(description = "The code of the duplicated task type.")
        private String taskTypeCode;

        public DuplicateTaskTypeCodeIssue() {
            this(null);
        }

        public DuplicateTaskTypeCodeIssue(String taskTypeCode) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.taskTypeCode = taskTypeCode;
        }

        public String getTaskTypeCode() {
            return taskTypeCode;
        }
    }

    @Schema(allOf = { TaskAssigningIssue.class })
    public static class DuplicateCustomerIdIssue extends TaskAssigningIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_CUSTOMER_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate customer ID found.");

        @Schema(description = "The ID of the duplicated customer.")
        private String customerId;

        public DuplicateCustomerIdIssue() {
            this(null);
        }

        public DuplicateCustomerIdIssue(String customerId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.customerId = customerId;
        }

        public String getCustomerId() {
            return customerId;
        }
    }

    @Schema(allOf = { TaskAssigningIssue.class })
    public static class NonExistingTaskTypeReferenceIssue extends TaskAssigningIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_TASK_TYPE_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Task refers to a task type code that does not exist.");

        @Schema(description = "The ID of the task with the unknown task type reference.")
        private String taskId;

        public NonExistingTaskTypeReferenceIssue() {
            this(null);
        }

        public NonExistingTaskTypeReferenceIssue(String taskId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.taskId = taskId;
        }

        public String getTaskId() {
            return taskId;
        }
    }

    @Schema(allOf = { TaskAssigningIssue.class })
    public static class NonExistingCustomerReferenceIssue extends TaskAssigningIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_CUSTOMER_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Task refers to a customer ID that does not exist.");

        @Schema(description = "The ID of the task with the unknown customer reference.")
        private String taskId;

        public NonExistingCustomerReferenceIssue() {
            this(null);
        }

        public NonExistingCustomerReferenceIssue(String taskId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.taskId = taskId;
        }

        public String getTaskId() {
            return taskId;
        }
    }

    @Schema(allOf = { TaskAssigningIssue.class })
    public static class NonExistingCustomerAffinityReferenceIssue extends TaskAssigningIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_CUSTOMER_AFFINITY_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Employee's customer affinities refer to a customer ID that does not exist.");

        @Schema(description = "The ID of the employee with the unknown customer affinity reference.")
        private String employeeId;

        public NonExistingCustomerAffinityReferenceIssue() {
            this(null);
        }

        public NonExistingCustomerAffinityReferenceIssue(String employeeId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.employeeId = employeeId;
        }

        public String getEmployeeId() {
            return employeeId;
        }
    }

    @Schema(allOf = { TaskAssigningIssue.class })
    public static class NonExistingTaskReferenceInAssignmentIssue extends TaskAssigningIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_TASK_REFERENCE_IN_ASSIGNMENT");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Employee's assigned task IDs refer to a task ID that does not exist.");

        @Schema(description = "The ID of the employee with the unknown task reference.")
        private String employeeId;

        public NonExistingTaskReferenceInAssignmentIssue() {
            this(null);
        }

        public NonExistingTaskReferenceInAssignmentIssue(String employeeId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.employeeId = employeeId;
        }

        public String getEmployeeId() {
            return employeeId;
        }
    }

    @Schema(allOf = { TaskAssigningIssue.class })
    public static class TaskAssignedToMultipleEmployeesIssue extends TaskAssigningIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("TASK_ASSIGNED_TO_MULTIPLE_EMPLOYEES");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage(
                "A task ID appears in the assigned task IDs of more than one employee, or twice for the same employee.");

        @Schema(description = "The ID of the task assigned more than once.")
        private String taskId;

        public TaskAssignedToMultipleEmployeesIssue() {
            this(null);
        }

        public TaskAssignedToMultipleEmployeesIssue(String taskId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.taskId = taskId;
        }

        public String getTaskId() {
            return taskId;
        }
    }
}
