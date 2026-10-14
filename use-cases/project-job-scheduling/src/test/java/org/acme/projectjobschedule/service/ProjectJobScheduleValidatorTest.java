package org.acme.projectjobschedule.service;

import static org.acme.projectjobschedule.support.TestHelper.aJobDTO;
import static org.acme.projectjobschedule.support.TestHelper.aResourceDTO;
import static org.acme.projectjobschedule.support.TestHelper.anAllocationDTO;
import static org.acme.projectjobschedule.support.TestHelper.createProblem;
import static org.acme.projectjobschedule.support.TestHelper.executionModeDTO;
import static org.acme.projectjobschedule.support.TestHelper.input;
import static org.acme.projectjobschedule.support.TestHelper.projectDTO;
import static org.acme.projectjobschedule.support.TestHelper.requirementDTO;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.Issue;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;
import ai.timefold.solver.service.definition.api.validation.dto.ValidationResult;

import org.acme.projectjobschedule.demo.DemoDataBuilder;
import org.acme.projectjobschedule.domain.JobType;
import org.acme.projectjobschedule.dto.input.AllocationInputDTO;
import org.acme.projectjobschedule.dto.input.JobInputDTO;
import org.acme.projectjobschedule.dto.input.ProjectJobScheduleInput;
import org.acme.projectjobschedule.dto.input.ResourceInputDTO;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.CrossProjectResourceRequirementIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.CyclicJobPrecedenceIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.DuplicateAllocationIdIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.DuplicateExecutionModeIdIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.DuplicateJobIdIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.DuplicateProjectIdIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.DuplicateResourceIdIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.DuplicateResourceRequirementIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.InvalidAllocationCountForJobIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.InvalidSourceOrSinkExecutionModeIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.NonExistingExecutionModeReferenceIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.NonExistingJobReferenceIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.NonExistingProjectReferenceIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.NonExistingResourceReferenceIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.PinnedAllocationWithoutAssignmentIssue;
import org.acme.projectjobschedule.service.validation.ProjectJobScheduleIssue.ProjectWithoutSourceOrSinkJobIssue;
import org.junit.jupiter.api.Test;

// OpenAPI spec compliance (Bean Validation) is enforced by the Service module at the REST layer, so it's
// covered by org.acme.projectjobschedule.rest.ProjectJobScheduleOpenApiValidationTest instead. This class
// only covers the domain-specific checks ProjectJobScheduleValidator implements itself.
class ProjectJobScheduleValidatorTest {

    private static final ProjectJobScheduleInput PROBLEM = createProblem();

    private final ProjectJobScheduleValidator validator = new ProjectJobScheduleValidator();

    @Test
    void validInputHasNoIssues() {
        assertThat(validate(PROBLEM).issues()).isEmpty();
    }

    @Test
    void demoDatasetHasNoIssues() {
        // The service must never ship demo data that its own validator would reject.
        assertThat(validate(DemoDataBuilder.builder().build()).issues()).isEmpty();
    }

    @Test
    void duplicateProjectId() {
        ProjectJobScheduleInput input = input(List.of(PROBLEM.projects().getFirst(), PROBLEM.projects().getFirst()),
                PROBLEM.resources(), PROBLEM.jobs(), PROBLEM.allocations());
        assertSingleIssue(validate(input), DuplicateProjectIdIssue.class);
    }

    @Test
    void duplicateResourceId() {
        ProjectJobScheduleInput input = input(PROBLEM.projects(),
                Stream.concat(PROBLEM.resources().stream(), Stream.of(PROBLEM.resources().getFirst())).toList(),
                PROBLEM.jobs(), PROBLEM.allocations());
        assertSingleIssue(validate(input), DuplicateResourceIdIssue.class);
    }

    @Test
    void duplicateJobId() {
        // Same job id, but its own execution mode id, so only the duplicated job id is reported.
        JobInputDTO duplicate = aJobDTO("A").executionModes(List.of(executionModeDTO("A-9", 1, List.of()))).build();
        assertSingleIssue(validate(withJobs(Stream.concat(PROBLEM.jobs().stream(), Stream.of(duplicate)).toList())),
                DuplicateJobIdIssue.class);
    }

    @Test
    void duplicateExecutionModeId() {
        JobInputDTO extraJob = aJobDTO("D").executionModes(List.of(executionModeDTO("A-1", 1, List.of()))).build();
        ProjectJobScheduleInput input = input(PROBLEM.projects(), PROBLEM.resources(),
                Stream.concat(PROBLEM.jobs().stream(), Stream.of(extraJob)).toList(),
                Stream.concat(PROBLEM.allocations().stream(), Stream.of(anAllocationDTO("D", "D").build())).toList());
        assertSingleIssue(validate(input), DuplicateExecutionModeIdIssue.class);
    }

    @Test
    void duplicateAllocationId() {
        // Job B's allocation reuses the id of job A's allocation, so every job still has exactly one.
        List<AllocationInputDTO> allocations = PROBLEM.allocations().stream()
                .map(allocation -> allocation.jobId().equals("B") ? anAllocationDTO("A", "B").build() : allocation)
                .toList();
        assertSingleIssue(validate(withAllocations(allocations)), DuplicateAllocationIdIssue.class);
    }

    @Test
    void duplicateResourceRequirement() {
        assertSingleIssue(validate(withJob(aJobDTO("A")
                .executionModes(List.of(executionModeDTO("A-1", 2,
                        List.of(requirementDTO("R1", 2), requirementDTO("R1", 3)))))
                .successorJobIds(List.of("C"))
                .build())), DuplicateResourceRequirementIssue.class);
    }

    @Test
    void crossProjectResourceRequirement() {
        // P2 is a complete project of its own, owning R3, so only job A of P1 requiring R3 is wrong.
        List<JobInputDTO> p2Jobs = List.of(
                aJobDTO("P2-source").projectId("P2").jobType(JobType.SOURCE)
                        .executionModes(List.of(executionModeDTO("P2-source-1", 0, List.of())))
                        .successorJobIds(List.of("P2-sink"))
                        .build(),
                aJobDTO("P2-sink").projectId("P2").jobType(JobType.SINK)
                        .executionModes(List.of(executionModeDTO("P2-sink-1", 0, List.of())))
                        .build());
        JobInputDTO jobA = aJobDTO("A")
                .executionModes(List.of(executionModeDTO("A-1", 2, List.of(requirementDTO("R3", 2)))))
                .successorJobIds(List.of("C"))
                .build();
        ProjectJobScheduleInput input = input(
                Stream.concat(PROBLEM.projects().stream(), Stream.of(projectDTO("P2", 0))).toList(),
                Stream.concat(PROBLEM.resources().stream(),
                        Stream.of(aResourceDTO("R3").projectId("P2").build())).toList(),
                Stream.concat(replace(PROBLEM.jobs(), jobA, JobInputDTO::id).stream(), p2Jobs.stream()).toList(),
                Stream.concat(PROBLEM.allocations().stream(),
                        p2Jobs.stream().map(job -> anAllocationDTO(job.id(), job.id()).build())).toList());
        assertSingleIssue(validate(input), CrossProjectResourceRequirementIssue.class);
    }

    @Test
    void nonExistingProjectReference() {
        ResourceInputDTO resource = aResourceDTO("R1").projectId("does-not-exist").build();
        ProjectJobScheduleInput input = input(PROBLEM.projects(),
                List.of(resource, PROBLEM.resources().getLast()), PROBLEM.jobs(), PROBLEM.allocations());
        assertSingleIssue(validate(input), NonExistingProjectReferenceIssue.class);
    }

    @Test
    void nonExistingResourceReference() {
        assertSingleIssue(validate(withJob(aJobDTO("A")
                .executionModes(List.of(executionModeDTO("A-1", 2, List.of(requirementDTO("does-not-exist", 2)))))
                .successorJobIds(List.of("C"))
                .build())), NonExistingResourceReferenceIssue.class);
    }

    @Test
    void nonExistingJobReference() {
        assertSingleIssue(validate(withJob(aJobDTO("C").successorJobIds(List.of("does-not-exist")).build())),
                NonExistingJobReferenceIssue.class);
    }

    @Test
    void nonExistingExecutionModeReference() {
        // B-1 is an execution mode of job B, so job A cannot be executed in it.
        assertSingleIssue(validate(withAllocation(anAllocationDTO("A", "A").executionModeId("B-1").delay(0).build())),
                NonExistingExecutionModeReferenceIssue.class);
    }

    @Test
    void invalidAllocationCountForJob() {
        assertSingleIssue(validate(withAllocations(PROBLEM.allocations().stream()
                .filter(allocation -> !allocation.jobId().equals("A"))
                .toList())), InvalidAllocationCountForJobIssue.class);
    }

    @Test
    void cyclicJobPrecedence() {
        // A precedes C, so C cannot precede A too.
        assertSingleIssue(validate(withJob(aJobDTO("C").successorJobIds(List.of("A")).build())),
                CyclicJobPrecedenceIssue.class);
    }

    @Test
    void projectWithoutSourceOrSinkJob() {
        assertSingleIssue(validate(withJob(aJobDTO("sink")
                .executionModes(List.of(executionModeDTO("sink-1", 0, List.of())))
                .build())), ProjectWithoutSourceOrSinkJobIssue.class);
    }

    @Test
    void sourceOrSinkJobWithPositiveDuration() {
        assertSingleIssue(validate(withJob(aJobDTO("sink").jobType(JobType.SINK)
                .executionModes(List.of(executionModeDTO("sink-1", 1, List.of())))
                .build())), InvalidSourceOrSinkExecutionModeIssue.class);
    }

    @Test
    void sourceOrSinkJobWithResourceRequirement() {
        assertSingleIssue(validate(withJob(aJobDTO("source").jobType(JobType.SOURCE)
                .executionModes(List.of(executionModeDTO("source-1", 0, List.of(requirementDTO("R1", 1)))))
                .successorJobIds(List.of("A", "B"))
                .build())), InvalidSourceOrSinkExecutionModeIssue.class);
    }

    @Test
    void sourceOrSinkJobWithMultipleExecutionModes() {
        assertSingleIssue(validate(withJob(aJobDTO("sink").jobType(JobType.SINK)
                .executionModes(List.of(executionModeDTO("sink-1", 0, List.of()),
                        executionModeDTO("sink-2", 0, List.of())))
                .build())), InvalidSourceOrSinkExecutionModeIssue.class);
    }

    @Test
    void pinnedAllocationWithoutAssignment() {
        assertSingleIssue(validate(withAllocation(anAllocationDTO("A", "A").pinned(true).build())),
                PinnedAllocationWithoutAssignmentIssue.class);
    }

    @Test
    void pinnedSourceOrSinkAllocationWithoutAssignmentIsAccepted() {
        // The model schedules a source or a sink job itself, ignoring what its allocation says.
        ProjectJobScheduleInput input = withAllocations(PROBLEM.allocations().stream()
                .map(allocation -> allocation.jobId().equals("source") || allocation.jobId().equals("sink")
                        ? anAllocationDTO(allocation.id(), allocation.jobId()).pinned(true).build()
                        : allocation)
                .toList());
        assertThat(validate(input).issues()).isEmpty();
    }

    @Test
    void mixedDatasetReportsEveryIssue() {
        ProjectJobScheduleInput input = input(
                List.of(PROBLEM.projects().getFirst(), PROBLEM.projects().getFirst()),
                List.of(aResourceDTO("R1").projectId("does-not-exist").build(), PROBLEM.resources().getLast()),
                replace(PROBLEM.jobs(), aJobDTO("C").successorJobIds(List.of("does-not-exist")).build(),
                        JobInputDTO::id),
                replace(PROBLEM.allocations(), anAllocationDTO("A", "A").pinned(true).build(),
                        AllocationInputDTO::id));

        Collection<Issue> issues = validate(input).issues();
        assertThat(issues).hasSize(4);
        assertThat(issues).hasAtLeastOneElementOfType(DuplicateProjectIdIssue.class)
                .hasAtLeastOneElementOfType(NonExistingProjectReferenceIssue.class)
                .hasAtLeastOneElementOfType(NonExistingJobReferenceIssue.class)
                .hasAtLeastOneElementOfType(PinnedAllocationWithoutAssignmentIssue.class);
    }

    private ProjectJobScheduleInput withJob(JobInputDTO job) {
        return withJobs(replace(PROBLEM.jobs(), job, JobInputDTO::id));
    }

    private ProjectJobScheduleInput withJobs(List<JobInputDTO> jobs) {
        return input(PROBLEM.projects(), PROBLEM.resources(), jobs, PROBLEM.allocations());
    }

    private ProjectJobScheduleInput withAllocation(AllocationInputDTO allocation) {
        return withAllocations(replace(PROBLEM.allocations(), allocation, AllocationInputDTO::id));
    }

    private ProjectJobScheduleInput withAllocations(List<AllocationInputDTO> allocations) {
        return input(PROBLEM.projects(), PROBLEM.resources(), PROBLEM.jobs(), allocations);
    }

    /** Replaces the element with the same id as the replacement, leaving the rest of the list as is. */
    private static <T> List<T> replace(List<T> elements, T replacement, java.util.function.Function<T, String> idOf) {
        return elements.stream()
                .map(element -> idOf.apply(element).equals(idOf.apply(replacement)) ? replacement : element)
                .toList();
    }

    private ValidationResult<Issue> validate(ProjectJobScheduleInput input) {
        ValidationBuilder validationBuilder = new ValidationBuilder();
        validator.validate(validationBuilder, input, ModelConfig.empty());
        return validationBuilder.build();
    }

    private static <T extends Issue> void assertSingleIssue(ValidationResult<Issue> result, Class<T> expectedType) {
        Collection<Issue> issues = result.issues();
        assertThat(issues).hasSize(1);
        assertThat(issues.iterator().next()).isInstanceOf(expectedType);
    }
}
