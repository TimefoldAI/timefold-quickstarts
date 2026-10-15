package org.acme.projectjobschedule.service;

import static org.acme.projectjobschedule.support.TestHelper.allocationOutputDTO;
import static org.acme.projectjobschedule.support.TestHelper.anAllocationDTO;
import static org.acme.projectjobschedule.support.TestHelper.createProblem;
import static org.acme.projectjobschedule.support.TestHelper.input;
import static org.acme.projectjobschedule.support.TestHelper.output;
import static org.acme.projectjobschedule.support.TestHelper.projectDTO;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.projectjobschedule.domain.Allocation;
import org.acme.projectjobschedule.domain.JobType;
import org.acme.projectjobschedule.domain.ProjectJobSchedule;
import org.acme.projectjobschedule.dto.input.AllocationInputDTO;
import org.acme.projectjobschedule.dto.input.ProjectJobScheduleInput;
import org.acme.projectjobschedule.dto.output.ProjectJobScheduleOutput;
import org.junit.jupiter.api.Test;

class ProjectJobScheduleModelConvertorTest {

    private static final ProjectJobScheduleInput PROBLEM = createProblem();

    private final ProjectJobScheduleModelConvertor convertor = new ProjectJobScheduleModelConvertor();

    @Test
    void unassignedInputLeavesTheRealJobsToTheSolver() {
        ProjectJobSchedule schedule = toSolverModel(PROBLEM);

        assertThat(schedule.getJobs()).hasSize(5);
        assertThat(schedule.getExecutionModes()).hasSize(8);
        assertThat(schedule.getResourceRequirements()).hasSize(12);
        assertThat(standardAllocations(schedule)).allSatisfy(allocation -> {
            assertThat(allocation.isPinned()).isFalse();
            assertThat(allocation.getExecutionMode()).isNull();
            assertThat(allocation.getDelay()).isNull();
        });
    }

    @Test
    void sourceAndSinkAllocationsArePinnedAndScheduled() {
        ProjectJobSchedule schedule = toSolverModel(PROBLEM);

        // A source or a sink job is not real work, so the model schedules it itself.
        assertThat(schedule.getAllocations())
                .filteredOn(allocation -> allocation.getJobType() != JobType.STANDARD)
                .hasSize(2)
                .allSatisfy(allocation -> {
                    assertThat(allocation.isPinned()).isTrue();
                    assertThat(allocation.getExecutionMode().duration()).isZero();
                    assertThat(allocation.getDelay()).isZero();
                });
    }

    @Test
    void pinnedAllocationKeepsWhatTheInputAssigned() {
        ProjectJobSchedule schedule = toSolverModel(withAllocation(
                anAllocationDTO("A", "A").executionModeId("A-2").delay(3).pinned(true).build()));

        Allocation allocation = allocationById(schedule, "A");
        assertThat(allocation.isPinned()).isTrue();
        assertThat(allocation.getExecutionMode().id()).isEqualTo("A-2");
        assertThat(allocation.getDelay()).isEqualTo(3);
    }

    @Test
    void allocationsAreLinkedByTheirJobsPrecedences() {
        ProjectJobSchedule schedule = toSolverModel(PROBLEM);

        Allocation source = allocationById(schedule, "source");
        Allocation jobA = allocationById(schedule, "A");
        Allocation sink = allocationById(schedule, "sink");

        assertThat(source.getPredecessorAllocations()).isEmpty();
        assertThat(source.getSuccessorAllocations()).containsExactly(jobA, allocationById(schedule, "B"));
        assertThat(jobA.getPredecessorAllocations()).containsExactly(source);
        assertThat(allocationById(schedule, "C").getPredecessorAllocations())
                .containsExactly(jobA, allocationById(schedule, "B"));
        assertThat(sink.getSourceAllocation()).isEqualTo(source);
        assertThat(sink.getSinkAllocation()).isEqualTo(sink);
        // The dates are seeded, even though nothing is scheduled yet.
        assertThat(source.getEndDate()).isZero();
        assertThat(sink.getEndDate()).isZero();
    }

    @Test
    void noJobStartsBeforeItsProjectIsReleased() {
        ProjectJobScheduleInput releasedLater = input(List.of(projectDTO(PROBLEM.projects().getFirst().id(), 3, 4)),
                PROBLEM.resources(), PROBLEM.jobs(), PROBLEM.allocations());
        ProjectJobSchedule schedule = toSolverModel(withAllocation(releasedLater,
                anAllocationDTO("A", "A").executionModeId("A-1").delay(0).build()));

        // The source is done on the release date, so even an undelayed first job only starts then.
        assertThat(allocationById(schedule, "source").getEndDate()).isEqualTo(3);
        Allocation jobA = allocationById(schedule, "A");
        assertThat(jobA.getStartDate()).isEqualTo(3);
        assertThat(jobA.getEndDate()).isEqualTo(5);
    }

    @Test
    void lastOutputRecoversTheAssignmentOfAHalfwayRun() {
        ProjectJobScheduleOutput lastModelOutput =
                output(List.of(allocationOutputDTO("A", "A-2", 1, 1, 5)));

        ProjectJobSchedule schedule =
                convertor.toSolverModel(PROBLEM, ModelConfig.empty(), Optional.of(lastModelOutput));

        Allocation allocation = allocationById(schedule, "A");
        assertThat(allocation.getExecutionMode().id()).isEqualTo("A-2");
        assertThat(allocation.getDelay()).isEqualTo(1);
        // Recomputed from the recovered assignment, not taken from the output.
        assertThat(allocation.getStartDate()).isEqualTo(1);
        assertThat(allocation.getEndDate()).isEqualTo(5);
        assertThat(allocationById(schedule, "B").getExecutionMode()).isNull();
    }

    @Test
    void outputRoundTripsBackOntoTheInput() {
        ProjectJobSchedule schedule = toSolverModel(withAllocation(
                anAllocationDTO("A", "A").executionModeId("A-1").delay(2).build()));

        ProjectJobScheduleOutput modelOutput = convertor.toModelOutput(schedule);
        assertThat(modelOutput.allocations()).hasSameSizeAs(PROBLEM.allocations());

        ProjectJobScheduleInput updatedInput = convertor.applyOutputToInput(PROBLEM, modelOutput);
        AllocationInputDTO updated = updatedInput.allocations().stream()
                .filter(allocation -> allocation.id().equals("A"))
                .findFirst()
                .orElseThrow();
        assertThat(updated.executionModeId()).isEqualTo("A-1");
        assertThat(updated.delay()).isEqualTo(2);
        assertThat(updatedInput.jobs()).isEqualTo(PROBLEM.jobs());
    }

    private ProjectJobSchedule toSolverModel(ProjectJobScheduleInput modelInput) {
        return convertor.toSolverModel(modelInput, ModelConfig.empty(), Optional.empty());
    }

    private static ProjectJobScheduleInput withAllocation(AllocationInputDTO replacement) {
        return withAllocation(PROBLEM, replacement);
    }

    private static ProjectJobScheduleInput withAllocation(ProjectJobScheduleInput modelInput,
            AllocationInputDTO replacement) {
        List<AllocationInputDTO> allocations = modelInput.allocations().stream()
                .map(allocation -> allocation.id().equals(replacement.id()) ? replacement : allocation)
                .toList();
        return input(modelInput.projects(), modelInput.resources(), modelInput.jobs(), allocations);
    }

    private static List<Allocation> standardAllocations(ProjectJobSchedule schedule) {
        return schedule.getAllocations().stream()
                .filter(allocation -> allocation.getJobType() == JobType.STANDARD)
                .toList();
    }

    private static Allocation allocationById(ProjectJobSchedule schedule, String id) {
        return schedule.getAllocations().stream()
                .filter(allocation -> allocation.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }
}
