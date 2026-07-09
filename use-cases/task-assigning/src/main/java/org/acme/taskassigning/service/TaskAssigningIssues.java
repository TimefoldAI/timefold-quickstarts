package org.acme.taskassigning.service;

import java.util.List;
import java.util.stream.Stream;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.acme.taskassigning.dto.EmployeeIdDetail;
import org.acme.taskassigning.dto.TaskAssigningValidationIssue;
import org.acme.taskassigning.dto.TaskIdDetail;

@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class TaskAssigningIssues {

    private TaskAssigningIssues() {
    }

    public abstract static class TaskAssigningIssue extends AbstractIssue {
        protected TaskAssigningIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
            super(code, severity, metadata);
        }
    }

    public static final class TaskIdMissingIssue extends TaskAssigningIssue {
        private static final IssueType TYPE = TaskAssigningValidationIssue.TASK_ID_MISSING.asIssueType();

        public TaskIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateTaskIdIssue extends TaskAssigningIssue {
        private static final IssueType TYPE = TaskAssigningValidationIssue.DUPLICATE_TASK_ID.asIssueType();

        public DuplicateTaskIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateTaskIdIssue(TaskIdDetail taskIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(taskIdDetail)).toList());
        }
    }

    public static final class EmployeeIdMissingIssue extends TaskAssigningIssue {
        private static final IssueType TYPE = TaskAssigningValidationIssue.EMPLOYEE_ID_MISSING.asIssueType();

        public EmployeeIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateEmployeeIdIssue extends TaskAssigningIssue {
        private static final IssueType TYPE = TaskAssigningValidationIssue.DUPLICATE_EMPLOYEE_ID.asIssueType();

        public DuplicateEmployeeIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateEmployeeIdIssue(EmployeeIdDetail employeeIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(employeeIdDetail)).toList());
        }
    }

    public static final class NonExistingTaskReferenceIssue extends TaskAssigningIssue {
        private static final IssueType TYPE = TaskAssigningValidationIssue.NON_EXISTING_TASK_REFERENCE.asIssueType();

        public NonExistingTaskReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingTaskReferenceIssue(TaskIdDetail taskIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(taskIdDetail)).toList());
        }
    }

    public static final class DuplicateTaskAssignmentIssue extends TaskAssigningIssue {
        private static final IssueType TYPE = TaskAssigningValidationIssue.DUPLICATE_TASK_ASSIGNMENT.asIssueType();

        public DuplicateTaskAssignmentIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateTaskAssignmentIssue(TaskIdDetail taskIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(taskIdDetail)).toList());
        }
    }
}
