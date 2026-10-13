package org.acme.projectjobschedule.service.validation;

import java.util.List;

import ai.timefold.solver.service.definition.api.validation.AbstractIssue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueMetadata;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.metadata.IssueMessage;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A dataset validation issue reported for a project job scheduling input.",
        oneOf = {
                ProjectJobScheduleIssue.DuplicateProjectIdIssue.class,
                ProjectJobScheduleIssue.DuplicateResourceIdIssue.class,
                ProjectJobScheduleIssue.DuplicateJobIdIssue.class,
                ProjectJobScheduleIssue.DuplicateExecutionModeIdIssue.class,
                ProjectJobScheduleIssue.DuplicateAllocationIdIssue.class,
                ProjectJobScheduleIssue.DuplicateResourceRequirementIssue.class,
                ProjectJobScheduleIssue.NonExistingProjectReferenceIssue.class,
                ProjectJobScheduleIssue.NonExistingResourceReferenceIssue.class,
                ProjectJobScheduleIssue.NonExistingJobReferenceIssue.class,
                ProjectJobScheduleIssue.NonExistingExecutionModeReferenceIssue.class,
                ProjectJobScheduleIssue.InvalidAllocationCountForJobIssue.class,
                ProjectJobScheduleIssue.CyclicJobPrecedenceIssue.class,
                ProjectJobScheduleIssue.ProjectWithoutSourceOrSinkJobIssue.class,
                ProjectJobScheduleIssue.PinnedAllocationWithoutAssignmentIssue.class
        })
public abstract class ProjectJobScheduleIssue extends AbstractIssue {

    protected ProjectJobScheduleIssue(IssueCode code, IssueSeverity severity, List<IssueMetadata> metadata) {
        super(code, severity, metadata);
    }

    @Schema(allOf = { ProjectJobScheduleIssue.class })
    public static class DuplicateProjectIdIssue extends ProjectJobScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_PROJECT_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate project ID found.");

        @Schema(description = "The ID of the duplicated project.")
        private String projectId;

        public DuplicateProjectIdIssue() {
            this(null);
        }

        public DuplicateProjectIdIssue(String projectId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.projectId = projectId;
        }

        public String getProjectId() {
            return projectId;
        }
    }

    @Schema(allOf = { ProjectJobScheduleIssue.class })
    public static class DuplicateResourceIdIssue extends ProjectJobScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_RESOURCE_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate resource ID found.");

        @Schema(description = "The ID of the duplicated resource.")
        private String resourceId;

        public DuplicateResourceIdIssue() {
            this(null);
        }

        public DuplicateResourceIdIssue(String resourceId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.resourceId = resourceId;
        }

        public String getResourceId() {
            return resourceId;
        }
    }

    @Schema(allOf = { ProjectJobScheduleIssue.class })
    public static class DuplicateJobIdIssue extends ProjectJobScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_JOB_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate job ID found.");

        @Schema(description = "The ID of the duplicated job.")
        private String jobId;

        public DuplicateJobIdIssue() {
            this(null);
        }

        public DuplicateJobIdIssue(String jobId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.jobId = jobId;
        }

        public String getJobId() {
            return jobId;
        }
    }

    @Schema(allOf = { ProjectJobScheduleIssue.class })
    public static class DuplicateExecutionModeIdIssue extends ProjectJobScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_EXECUTION_MODE_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate execution mode ID found.");

        @Schema(description = "The ID of the duplicated execution mode.")
        private String executionModeId;

        public DuplicateExecutionModeIdIssue() {
            this(null);
        }

        public DuplicateExecutionModeIdIssue(String executionModeId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.executionModeId = executionModeId;
        }

        public String getExecutionModeId() {
            return executionModeId;
        }
    }

    @Schema(allOf = { ProjectJobScheduleIssue.class })
    public static class DuplicateAllocationIdIssue extends ProjectJobScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_ALLOCATION_ID");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Duplicate allocation ID found.");

        @Schema(description = "The ID of the duplicated allocation.")
        private String allocationId;

        public DuplicateAllocationIdIssue() {
            this(null);
        }

        public DuplicateAllocationIdIssue(String allocationId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.allocationId = allocationId;
        }

        public String getAllocationId() {
            return allocationId;
        }
    }

    @Schema(allOf = { ProjectJobScheduleIssue.class })
    public static class DuplicateResourceRequirementIssue extends ProjectJobScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("DUPLICATE_RESOURCE_REQUIREMENT");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Execution mode requires the same resource more than once.");

        @Schema(description = "The ID of the execution mode with the duplicated requirement.")
        private String executionModeId;
        @Schema(description = "The ID of the resource required more than once.")
        private String resourceId;

        public DuplicateResourceRequirementIssue() {
            this(null, null);
        }

        public DuplicateResourceRequirementIssue(String executionModeId, String resourceId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.executionModeId = executionModeId;
            this.resourceId = resourceId;
        }

        public String getExecutionModeId() {
            return executionModeId;
        }

        public String getResourceId() {
            return resourceId;
        }
    }

    @Schema(allOf = { ProjectJobScheduleIssue.class })
    public static class NonExistingProjectReferenceIssue extends ProjectJobScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_PROJECT_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Reference to a project that does not exist.");

        @Schema(description = "The project ID that no project in the dataset has.")
        private String projectId;

        public NonExistingProjectReferenceIssue() {
            this(null);
        }

        public NonExistingProjectReferenceIssue(String projectId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.projectId = projectId;
        }

        public String getProjectId() {
            return projectId;
        }
    }

    @Schema(allOf = { ProjectJobScheduleIssue.class })
    public static class NonExistingResourceReferenceIssue extends ProjectJobScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_RESOURCE_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Resource requirement refers to a resource that does not exist.");

        @Schema(description = "The resource ID that no resource in the dataset has.")
        private String resourceId;

        public NonExistingResourceReferenceIssue() {
            this(null);
        }

        public NonExistingResourceReferenceIssue(String resourceId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.resourceId = resourceId;
        }

        public String getResourceId() {
            return resourceId;
        }
    }

    @Schema(allOf = { ProjectJobScheduleIssue.class })
    public static class NonExistingJobReferenceIssue extends ProjectJobScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_JOB_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Reference to a job that does not exist.");

        @Schema(description = "The job ID that no job in the dataset has.")
        private String jobId;

        public NonExistingJobReferenceIssue() {
            this(null);
        }

        public NonExistingJobReferenceIssue(String jobId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.jobId = jobId;
        }

        public String getJobId() {
            return jobId;
        }
    }

    @Schema(allOf = { ProjectJobScheduleIssue.class })
    public static class NonExistingExecutionModeReferenceIssue extends ProjectJobScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("NON_EXISTING_EXECUTION_MODE_REFERENCE");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Allocation refers to an execution mode that its job does not have.");

        @Schema(description = "The ID of the allocation with the unknown execution mode reference.")
        private String allocationId;

        public NonExistingExecutionModeReferenceIssue() {
            this(null);
        }

        public NonExistingExecutionModeReferenceIssue(String allocationId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.allocationId = allocationId;
        }

        public String getAllocationId() {
            return allocationId;
        }
    }

    @Schema(allOf = { ProjectJobScheduleIssue.class })
    public static class InvalidAllocationCountForJobIssue extends ProjectJobScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("INVALID_ALLOCATION_COUNT_FOR_JOB");
        public static final IssueMessage ISSUE_MESSAGE = new IssueMessage("Job is not scheduled by exactly one allocation.");

        @Schema(description = "The ID of the job that is not scheduled by exactly one allocation.")
        private String jobId;
        @Schema(description = "The number of allocations that schedule this job.")
        private int allocationCount;

        public InvalidAllocationCountForJobIssue() {
            this(null, 0);
        }

        public InvalidAllocationCountForJobIssue(String jobId, int allocationCount) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.jobId = jobId;
            this.allocationCount = allocationCount;
        }

        public String getJobId() {
            return jobId;
        }

        public int getAllocationCount() {
            return allocationCount;
        }
    }

    @Schema(allOf = { ProjectJobScheduleIssue.class })
    public static class CyclicJobPrecedenceIssue extends ProjectJobScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("CYCLIC_JOB_PRECEDENCE");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("The job precedence relations contain a cycle, so no job on it can ever start.");

        @Schema(description = "The ID of a job on the cycle.")
        private String jobId;

        public CyclicJobPrecedenceIssue() {
            this(null);
        }

        public CyclicJobPrecedenceIssue(String jobId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.jobId = jobId;
        }

        public String getJobId() {
            return jobId;
        }
    }

    @Schema(allOf = { ProjectJobScheduleIssue.class })
    public static class ProjectWithoutSourceOrSinkJobIssue extends ProjectJobScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("PROJECT_WITHOUT_SOURCE_OR_SINK_JOB");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Project does not have exactly one SOURCE job and one SINK job.");

        @Schema(description = "The ID of the project that is not bracketed by a source and a sink job.")
        private String projectId;

        public ProjectWithoutSourceOrSinkJobIssue() {
            this(null);
        }

        public ProjectWithoutSourceOrSinkJobIssue(String projectId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.projectId = projectId;
        }

        public String getProjectId() {
            return projectId;
        }
    }

    @Schema(allOf = { ProjectJobScheduleIssue.class })
    public static class PinnedAllocationWithoutAssignmentIssue extends ProjectJobScheduleIssue {

        public static final IssueCode ISSUE_CODE = IssueCode.of("PINNED_ALLOCATION_WITHOUT_ASSIGNMENT");
        public static final IssueMessage ISSUE_MESSAGE =
                new IssueMessage("Pinned allocation needs both an execution mode and a delay.");

        @Schema(description = "The ID of the pinned allocation without a complete assignment.")
        private String allocationId;

        public PinnedAllocationWithoutAssignmentIssue() {
            this(null);
        }

        public PinnedAllocationWithoutAssignmentIssue(String allocationId) {
            super(ISSUE_CODE, IssueSeverity.ERROR, List.of(ISSUE_MESSAGE));
            this.allocationId = allocationId;
        }

        public String getAllocationId() {
            return allocationId;
        }
    }
}
