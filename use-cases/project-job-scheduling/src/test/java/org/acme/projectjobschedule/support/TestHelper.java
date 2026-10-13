package org.acme.projectjobschedule.support;

import java.util.List;

import org.acme.projectjobschedule.domain.Allocation;
import org.acme.projectjobschedule.domain.ExecutionMode;
import org.acme.projectjobschedule.domain.Job;
import org.acme.projectjobschedule.domain.JobType;
import org.acme.projectjobschedule.domain.Project;
import org.acme.projectjobschedule.domain.Resource;
import org.acme.projectjobschedule.domain.ResourceRequirement;
import org.acme.projectjobschedule.dto.input.AllocationInputDTO;
import org.acme.projectjobschedule.dto.input.ExecutionModeInputDTO;
import org.acme.projectjobschedule.dto.input.JobInputDTO;
import org.acme.projectjobschedule.dto.input.ProjectInputDTO;
import org.acme.projectjobschedule.dto.input.ProjectJobScheduleInput;
import org.acme.projectjobschedule.dto.input.ResourceInputDTO;
import org.acme.projectjobschedule.dto.input.ResourceRequirementInputDTO;
import org.acme.projectjobschedule.dto.output.AllocationOutputDTO;
import org.acme.projectjobschedule.dto.output.ProjectJobScheduleOutput;

// To keep our production classes as simple as possible, we've added these methods to help construct the data needed for testing.
public final class TestHelper {

    public static final String PROJECT_ID = "P1";

    private TestHelper() {
    }

    // ************************************************************************
    // DTO factories
    // ************************************************************************

    public static ProjectJobScheduleInput input(List<ProjectInputDTO> projects, List<ResourceInputDTO> resources,
            List<JobInputDTO> jobs, List<AllocationInputDTO> allocations) {
        return new ProjectJobScheduleInput(projects, resources, jobs, allocations);
    }

    public static ProjectInputDTO projectDTO(String id, int criticalPathDuration) {
        return projectDTO(id, 0, criticalPathDuration);
    }

    public static ProjectInputDTO projectDTO(String id, int releaseDate, int criticalPathDuration) {
        return new ProjectInputDTO(id, releaseDate, criticalPathDuration);
    }

    public static ResourceDTOBuilder aResourceDTO(String id) {
        return new ResourceDTOBuilder(id);
    }

    public static ResourceRequirementInputDTO requirementDTO(String resourceId, int requirement) {
        return new ResourceRequirementInputDTO(resourceId, requirement);
    }

    public static ExecutionModeInputDTO executionModeDTO(String id, int duration,
            List<ResourceRequirementInputDTO> resourceRequirements) {
        return new ExecutionModeInputDTO(id, duration, resourceRequirements);
    }

    public static JobDTOBuilder aJobDTO(String id) {
        return new JobDTOBuilder(id);
    }

    public static AllocationDTOBuilder anAllocationDTO(String id, String jobId) {
        return new AllocationDTOBuilder(id, jobId);
    }

    /**
     * A small but complete problem: one project of three real jobs, where the two independent ones can
     * run in parallel without overbooking the renewable resource they share. Feasible by construction,
     * and its critical path is reachable, so a solve over it must reach a feasible score without delay.
     */
    public static ProjectJobScheduleInput createProblem() {
        List<ProjectInputDTO> projects = List.of(projectDTO(PROJECT_ID, 4));
        List<ResourceInputDTO> resources = List.of(
                aResourceDTO("R1").capacity(4).renewable(true).build(),
                aResourceDTO("R2").capacity(20).projectId(PROJECT_ID).renewable(false).build());
        List<JobInputDTO> jobs = List.of(
                aJobDTO("source").jobType(JobType.SOURCE)
                        .executionModes(List.of(executionModeDTO("source-1", 0, List.of())))
                        .successorJobIds(List.of("A", "B"))
                        .build(),
                aJobDTO("A").executionModes(standardExecutionModes("A")).successorJobIds(List.of("C")).build(),
                aJobDTO("B").executionModes(standardExecutionModes("B")).successorJobIds(List.of("C")).build(),
                aJobDTO("C").executionModes(standardExecutionModes("C")).successorJobIds(List.of("sink")).build(),
                aJobDTO("sink").jobType(JobType.SINK)
                        .executionModes(List.of(executionModeDTO("sink-1", 0, List.of())))
                        .build());
        List<AllocationInputDTO> allocations = jobs.stream()
                .map(job -> anAllocationDTO(job.id(), job.id()).build())
                .toList();
        return input(projects, resources, jobs, allocations);
    }

    /** Two ways of executing a job: a short one that needs more of the shared resource, and a long one. */
    private static List<ExecutionModeInputDTO> standardExecutionModes(String jobId) {
        return List.of(
                executionModeDTO(jobId + "-1", 2, List.of(requirementDTO("R1", 2), requirementDTO("R2", 3))),
                executionModeDTO(jobId + "-2", 4, List.of(requirementDTO("R1", 1), requirementDTO("R2", 2))));
    }

    public static ProjectJobScheduleOutput output(List<AllocationOutputDTO> allocations) {
        return new ProjectJobScheduleOutput(allocations);
    }

    public static AllocationOutputDTO allocationOutputDTO(String id, String executionModeId, Integer delay,
            Integer startDate, Integer endDate) {
        return new AllocationOutputDTO(id, executionModeId, delay, startDate, endDate);
    }

    // ************************************************************************
    // Solver model factories
    // ************************************************************************

    public static Project aProject(String id, int criticalPathDuration) {
        return new Project(id, 0, criticalPathDuration);
    }

    public static Resource aResource(String id, int capacity, boolean renewable) {
        return new Resource(id, capacity, null, renewable);
    }

    public static ExecutionMode anExecutionMode(String id, int duration) {
        return new ExecutionMode(id, duration);
    }

    public static ResourceRequirement aResourceRequirement(ExecutionMode executionMode, Resource resource,
            int requirement) {
        return new ResourceRequirement("%s-%s".formatted(executionMode.id(), resource.id()), executionMode, resource,
                requirement);
    }

    public static JobBuilder aJob(String id) {
        return new JobBuilder(id);
    }

    public static AllocationBuilder anAllocation(String id, Job job) {
        return new AllocationBuilder(id, job);
    }

    public static final class JobBuilder {

        private final String id;
        private Project project = aProject(PROJECT_ID, 4);
        private JobType jobType = JobType.STANDARD;
        private List<ExecutionMode> executionModes = List.of();

        private JobBuilder(String id) {
            this.id = id;
        }

        public JobBuilder project(Project project) {
            this.project = project;
            return this;
        }

        public JobBuilder jobType(JobType jobType) {
            this.jobType = jobType;
            return this;
        }

        public JobBuilder executionModes(List<ExecutionMode> executionModes) {
            this.executionModes = executionModes;
            return this;
        }

        public Job build() {
            return new Job(id, project, jobType, executionModes);
        }
    }

    /**
     * Builds an allocation with its dates already filled in, the way the solver would once it has
     * assigned the execution mode and the delay: a ConstraintVerifier does not recompute shadow
     * variables itself.
     */
    public static final class AllocationBuilder {

        private final String id;
        private final Job job;
        private ExecutionMode executionMode;
        private Integer delay = 0;
        private int predecessorsDoneDate = 0;

        private AllocationBuilder(String id, Job job) {
            this.job = job;
            this.id = id;
        }

        public AllocationBuilder executionMode(ExecutionMode executionMode) {
            this.executionMode = executionMode;
            return this;
        }

        public AllocationBuilder delay(Integer delay) {
            this.delay = delay;
            return this;
        }

        /** The date every job this one depends on is done, so the earliest date it can start on. */
        public AllocationBuilder predecessorsDoneDate(int predecessorsDoneDate) {
            this.predecessorsDoneDate = predecessorsDoneDate;
            return this;
        }

        public Allocation build() {
            Allocation allocation = new Allocation(id, job);
            allocation.setExecutionMode(executionMode);
            allocation.setDelay(delay);
            allocation.setPredecessorAllocations(List.of());
            allocation.setSuccessorAllocations(List.of());
            allocation.setPredecessorsDoneDate(predecessorsDoneDate);
            allocation.updateShadowsAfterPredecessorDoneDate();
            return allocation;
        }
    }

    // ************************************************************************
    // DTO builders
    // ************************************************************************

    public static final class ResourceDTOBuilder {

        private final String id;
        private int capacity = 10;
        private String projectId;
        private boolean renewable = true;

        private ResourceDTOBuilder(String id) {
            this.id = id;
        }

        public ResourceDTOBuilder capacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        public ResourceDTOBuilder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public ResourceDTOBuilder renewable(boolean renewable) {
            this.renewable = renewable;
            return this;
        }

        public ResourceInputDTO build() {
            return new ResourceInputDTO(id, capacity, projectId, renewable);
        }
    }

    public static final class JobDTOBuilder {

        private final String id;
        private String projectId = PROJECT_ID;
        private JobType jobType = JobType.STANDARD;
        private List<ExecutionModeInputDTO> executionModes;
        private List<String> successorJobIds = List.of();

        private JobDTOBuilder(String id) {
            this.id = id;
            this.executionModes = List.of(executionModeDTO(id + "-1", 1, List.of()));
        }

        public JobDTOBuilder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public JobDTOBuilder jobType(JobType jobType) {
            this.jobType = jobType;
            return this;
        }

        public JobDTOBuilder executionModes(List<ExecutionModeInputDTO> executionModes) {
            this.executionModes = executionModes;
            return this;
        }

        public JobDTOBuilder successorJobIds(List<String> successorJobIds) {
            this.successorJobIds = successorJobIds;
            return this;
        }

        public JobInputDTO build() {
            return new JobInputDTO(id, projectId, jobType, executionModes, successorJobIds);
        }
    }

    public static final class AllocationDTOBuilder {

        private final String id;
        private final String jobId;
        private String executionModeId;
        private Integer delay;
        private Boolean pinned;

        private AllocationDTOBuilder(String id, String jobId) {
            this.id = id;
            this.jobId = jobId;
        }

        public AllocationDTOBuilder executionModeId(String executionModeId) {
            this.executionModeId = executionModeId;
            return this;
        }

        public AllocationDTOBuilder delay(Integer delay) {
            this.delay = delay;
            return this;
        }

        public AllocationDTOBuilder pinned(Boolean pinned) {
            this.pinned = pinned;
            return this;
        }

        public AllocationInputDTO build() {
            return new AllocationInputDTO(id, jobId, executionModeId, delay, pinned);
        }
    }
}
