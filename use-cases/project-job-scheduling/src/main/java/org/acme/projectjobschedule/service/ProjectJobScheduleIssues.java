package org.acme.projectjobschedule.service;

import java.util.List;
import java.util.stream.Stream;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.IssueType;

import org.acme.projectjobschedule.dto.AllocationIdDetail;
import org.acme.projectjobschedule.dto.JobIdDetail;
import org.acme.projectjobschedule.dto.ProjectJobScheduleValidationIssue;
import org.acme.projectjobschedule.dto.ResourceIdDetail;

@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class ProjectJobScheduleIssues {

    private ProjectJobScheduleIssues() {
    }

    public abstract static class ProjectJobScheduleIssue extends AbstractIssue {
        protected ProjectJobScheduleIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
            super(code, severity, metadata);
        }
    }

    public static final class JobIdMissingIssue extends ProjectJobScheduleIssue {
        private static final IssueType TYPE = ProjectJobScheduleValidationIssue.JOB_ID_MISSING.asIssueType();

        public JobIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateJobIdIssue extends ProjectJobScheduleIssue {
        private static final IssueType TYPE = ProjectJobScheduleValidationIssue.DUPLICATE_JOB_ID.asIssueType();

        public DuplicateJobIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateJobIdIssue(JobIdDetail jobIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(jobIdDetail)).toList());
        }
    }

    public static final class ResourceIdMissingIssue extends ProjectJobScheduleIssue {
        private static final IssueType TYPE = ProjectJobScheduleValidationIssue.RESOURCE_ID_MISSING.asIssueType();

        public ResourceIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateResourceIdIssue extends ProjectJobScheduleIssue {
        private static final IssueType TYPE = ProjectJobScheduleValidationIssue.DUPLICATE_RESOURCE_ID.asIssueType();

        public DuplicateResourceIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateResourceIdIssue(ResourceIdDetail resourceIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(resourceIdDetail)).toList());
        }
    }

    public static final class AllocationIdMissingIssue extends ProjectJobScheduleIssue {
        private static final IssueType TYPE = ProjectJobScheduleValidationIssue.ALLOCATION_ID_MISSING.asIssueType();

        public AllocationIdMissingIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }
    }

    public static final class DuplicateAllocationIdIssue extends ProjectJobScheduleIssue {
        private static final IssueType TYPE = ProjectJobScheduleValidationIssue.DUPLICATE_ALLOCATION_ID.asIssueType();

        public DuplicateAllocationIdIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        DuplicateAllocationIdIssue(AllocationIdDetail allocationIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(allocationIdDetail)).toList());
        }
    }

    public static final class NonExistingJobReferenceIssue extends ProjectJobScheduleIssue {
        private static final IssueType TYPE = ProjectJobScheduleValidationIssue.NON_EXISTING_JOB_REFERENCE.asIssueType();

        public NonExistingJobReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingJobReferenceIssue(AllocationIdDetail allocationIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(allocationIdDetail)).toList());
        }
    }

    public static final class NonExistingExecutionModeReferenceIssue extends ProjectJobScheduleIssue {
        private static final IssueType TYPE =
                ProjectJobScheduleValidationIssue.NON_EXISTING_EXECUTION_MODE_REFERENCE.asIssueType();

        public NonExistingExecutionModeReferenceIssue() {
            super(TYPE.code(), TYPE.severity(), TYPE.metadata());
        }

        NonExistingExecutionModeReferenceIssue(AllocationIdDetail allocationIdDetail) {
            super(TYPE.code(), TYPE.severity(),
                    Stream.concat(TYPE.metadata().stream(), Stream.of(allocationIdDetail)).toList());
        }
    }
}
