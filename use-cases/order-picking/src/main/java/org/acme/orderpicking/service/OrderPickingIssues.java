package org.acme.orderpicking.service;

import java.util.List;
import java.util.stream.Stream;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.acme.orderpicking.dto.OrderPickingValidationIssue;
import org.acme.orderpicking.dto.PickTaskIdDetail;
import org.acme.orderpicking.dto.TrolleyIdDetail;

@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class OrderPickingIssues {

    private OrderPickingIssues() {
    }

    public abstract static class OrderPickingIssue extends AbstractIssue {
        protected OrderPickingIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
            super(code, severity, metadata);
        }
    }

    public static final class TrolleyIdMissingIssue extends OrderPickingIssue {
        private static final IssueType TYPE = OrderPickingValidationIssue.TROLLEY_ID_MISSING.asIssueType();

        public TrolleyIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateTrolleyIdIssue extends OrderPickingIssue {
        private static final IssueType TYPE = OrderPickingValidationIssue.DUPLICATE_TROLLEY_ID.asIssueType();

        public DuplicateTrolleyIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateTrolleyIdIssue(TrolleyIdDetail trolleyIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(trolleyIdDetail)).toList());
        }
    }

    public static final class PickTaskIdMissingIssue extends OrderPickingIssue {
        private static final IssueType TYPE = OrderPickingValidationIssue.PICK_TASK_ID_MISSING.asIssueType();

        public PickTaskIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicatePickTaskIdIssue extends OrderPickingIssue {
        private static final IssueType TYPE = OrderPickingValidationIssue.DUPLICATE_PICK_TASK_ID.asIssueType();

        public DuplicatePickTaskIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicatePickTaskIdIssue(PickTaskIdDetail pickTaskIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(pickTaskIdDetail)).toList());
        }
    }

    public static final class NonExistingPickTaskReferenceIssue extends OrderPickingIssue {
        private static final IssueType TYPE = OrderPickingValidationIssue.NON_EXISTING_PICK_TASK_REFERENCE.asIssueType();

        public NonExistingPickTaskReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingPickTaskReferenceIssue(PickTaskIdDetail pickTaskIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(pickTaskIdDetail)).toList());
        }
    }

    public static final class DuplicatePickTaskAssignmentIssue extends OrderPickingIssue {
        private static final IssueType TYPE = OrderPickingValidationIssue.DUPLICATE_PICK_TASK_ASSIGNMENT.asIssueType();

        public DuplicatePickTaskAssignmentIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicatePickTaskAssignmentIssue(PickTaskIdDetail pickTaskIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(pickTaskIdDetail)).toList());
        }
    }
}
