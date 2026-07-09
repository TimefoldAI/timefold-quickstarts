package org.acme.foodpackaging.service;

import java.util.List;
import java.util.stream.Stream;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.acme.foodpackaging.dto.JobIdDetail;
import org.acme.foodpackaging.dto.LineIdDetail;
import org.acme.foodpackaging.dto.PackagingScheduleValidationIssue;

@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class PackagingScheduleIssues {

    private PackagingScheduleIssues() {
    }

    public abstract static class PackagingScheduleIssue extends AbstractIssue {
        protected PackagingScheduleIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
            super(code, severity, metadata);
        }
    }

    public static final class JobIdMissingIssue extends PackagingScheduleIssue {
        private static final IssueType TYPE = PackagingScheduleValidationIssue.JOB_ID_MISSING.asIssueType();

        public JobIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateJobIdIssue extends PackagingScheduleIssue {
        private static final IssueType TYPE = PackagingScheduleValidationIssue.DUPLICATE_JOB_ID.asIssueType();

        public DuplicateJobIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateJobIdIssue(JobIdDetail jobIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(jobIdDetail)).toList());
        }
    }

    public static final class LineIdMissingIssue extends PackagingScheduleIssue {
        private static final IssueType TYPE = PackagingScheduleValidationIssue.LINE_ID_MISSING.asIssueType();

        public LineIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateLineIdIssue extends PackagingScheduleIssue {
        private static final IssueType TYPE = PackagingScheduleValidationIssue.DUPLICATE_LINE_ID.asIssueType();

        public DuplicateLineIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateLineIdIssue(LineIdDetail lineIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(lineIdDetail)).toList());
        }
    }

    public static final class NonExistingLineReferenceIssue extends PackagingScheduleIssue {
        private static final IssueType TYPE = PackagingScheduleValidationIssue.NON_EXISTING_LINE_REFERENCE.asIssueType();

        public NonExistingLineReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingLineReferenceIssue(JobIdDetail jobIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(jobIdDetail)).toList());
        }
    }

    public static final class NonExistingProductReferenceIssue extends PackagingScheduleIssue {
        private static final IssueType TYPE =
                PackagingScheduleValidationIssue.NON_EXISTING_PRODUCT_REFERENCE.asIssueType();

        public NonExistingProductReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingProductReferenceIssue(JobIdDetail jobIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(jobIdDetail)).toList());
        }
    }
}
