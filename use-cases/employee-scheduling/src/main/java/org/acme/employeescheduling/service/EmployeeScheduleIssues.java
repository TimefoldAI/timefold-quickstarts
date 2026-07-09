package org.acme.employeescheduling.service;

import java.util.List;
import java.util.stream.Stream;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.acme.employeescheduling.dto.EmployeeIdDetail;
import org.acme.employeescheduling.dto.EmployeeScheduleValidationIssue;
import org.acme.employeescheduling.dto.ShiftIdDetail;

@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class EmployeeScheduleIssues {

    private EmployeeScheduleIssues() {
    }

    public abstract static class EmployeeScheduleIssue extends AbstractIssue {

        protected EmployeeScheduleIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
            super(code, severity, metadata);
        }
    }

    public static final class EmployeeIdMissingIssue extends EmployeeScheduleIssue {

        private static final IssueType TYPE = EmployeeScheduleValidationIssue.EMPLOYEE_ID_MISSING.asIssueType();

        public EmployeeIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateEmployeeIdIssue extends EmployeeScheduleIssue {

        private static final IssueType TYPE = EmployeeScheduleValidationIssue.DUPLICATE_EMPLOYEE_ID.asIssueType();

        public DuplicateEmployeeIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateEmployeeIdIssue(EmployeeIdDetail detail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(detail)).toList());
        }
    }

    public static final class ShiftIdMissingIssue extends EmployeeScheduleIssue {

        private static final IssueType TYPE = EmployeeScheduleValidationIssue.SHIFT_ID_MISSING.asIssueType();

        public ShiftIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateShiftIdIssue extends EmployeeScheduleIssue {

        private static final IssueType TYPE = EmployeeScheduleValidationIssue.DUPLICATE_SHIFT_ID.asIssueType();

        public DuplicateShiftIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateShiftIdIssue(ShiftIdDetail detail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(detail)).toList());
        }
    }

    public static final class NonExistingEmployeeReferenceIssue extends EmployeeScheduleIssue {

        private static final IssueType TYPE =
                EmployeeScheduleValidationIssue.NON_EXISTING_EMPLOYEE_REFERENCE.asIssueType();

        public NonExistingEmployeeReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingEmployeeReferenceIssue(ShiftIdDetail detail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(detail)).toList());
        }
    }
}
