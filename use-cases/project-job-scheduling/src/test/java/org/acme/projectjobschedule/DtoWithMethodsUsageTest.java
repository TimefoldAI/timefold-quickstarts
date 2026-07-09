package org.acme.projectjobschedule;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.acme.projectjobschedule.dto.AllocationDTO;
import org.acme.projectjobschedule.dto.AllocationIdDetail;
import org.acme.projectjobschedule.dto.ExecutionModeDTO;
import org.acme.projectjobschedule.dto.JobDTO;
import org.acme.projectjobschedule.dto.JobIdDetail;
import org.acme.projectjobschedule.dto.ProjectDTO;
import org.acme.projectjobschedule.dto.ProjectJobScheduleConfigOverrides;
import org.acme.projectjobschedule.dto.ProjectJobScheduleInput;
import org.acme.projectjobschedule.dto.ProjectJobScheduleInputMetrics;
import org.acme.projectjobschedule.dto.ProjectJobScheduleOutput;
import org.acme.projectjobschedule.dto.ProjectJobScheduleOutputMetrics;
import org.acme.projectjobschedule.dto.ResourceDTO;
import org.acme.projectjobschedule.dto.ResourceIdDetail;
import org.acme.projectjobschedule.dto.ResourceRequirementDTO;
import org.junit.jupiter.api.Test;

class DtoWithMethodsUsageTest {

    @Test
    void allWithMethodsProduceUpdatedCopies() {
        var baseProject = new ProjectDTO("p1", 0, 10);
        var updatedProject = baseProject.withId("p2").withReleaseDate(4).withCriticalPathDuration(19);

        var baseResource = new ResourceDTO("r1", "GLOBAL", 16, true, "");
        var updatedResource = baseResource.withId("r2").withResourceType("LOCAL").withCapacity(20)
                .withRenewable(false).withProjectId("p2");

        var baseJob = new JobDTO("j1", "p1", "STANDARD", List.of());
        var updatedJob = baseJob.withId("j2").withProjectId("p2").withJobType("SINK")
                .withSuccessorJobIds(List.of("j3"));

        var baseExecutionMode = new ExecutionModeDTO("e1", "j1", 3);
        var updatedExecutionMode = baseExecutionMode.withId("e2").withJobId("j2").withDuration(5);

        var baseRequirement = new ResourceRequirementDTO("rr1", "e1", "r1", 2);
        var updatedRequirement = baseRequirement.withId("rr2").withExecutionModeId("e2").withResourceId("r2")
                .withRequirement(7);

        var baseAllocation =
                new AllocationDTO("a1", "j1", "s1", "k1", List.of("a0"), List.of("a2"), "e1", 1, 2, 3);
        var updatedAllocation = baseAllocation.withId("a2").withJobId("j2").withSourceAllocationId("s2")
                .withSinkAllocationId("k2").withPredecessorAllocationIds(List.of("a1"))
                .withSuccessorAllocationIds(List.of("a3")).withExecutionModeId("e2").withDelay(4)
                .withStartDate(5).withEndDate(6);

        var updatedJobIdDetail = new JobIdDetail("j1").withJobId("j2");
        var updatedResourceIdDetail = new ResourceIdDetail("r1").withResourceId("r2");
        var updatedAllocationIdDetail = new AllocationIdDetail("a1").withAllocationId("a2");

        var updatedOverrides = new ProjectJobScheduleConfigOverrides()
                .withTotalProjectDelayWeight(10L)
                .withTotalMakespanWeight(20L);

        var updatedInput = new ProjectJobScheduleInput(List.of(baseProject), List.of(baseResource), List.of(baseJob),
                List.of(baseExecutionMode), List.of(baseRequirement), List.of(baseAllocation))
                .withProjects(List.of(updatedProject))
                .withResources(List.of(updatedResource))
                .withJobs(List.of(updatedJob))
                .withExecutionModes(List.of(updatedExecutionMode))
                .withResourceRequirements(List.of(updatedRequirement))
                .withAllocations(List.of(updatedAllocation));

        var updatedOutput = new ProjectJobScheduleOutput(List.of(baseProject), List.of(baseResource), List.of(baseJob),
                List.of(baseExecutionMode), List.of(baseRequirement), List.of(baseAllocation), "0hard/0medium/0soft")
                .withProjects(List.of(updatedProject))
                .withResources(List.of(updatedResource))
                .withJobs(List.of(updatedJob))
                .withExecutionModes(List.of(updatedExecutionMode))
                .withResourceRequirements(List.of(updatedRequirement))
                .withAllocations(List.of(updatedAllocation))
                .withScore("0hard/0medium/1soft");

        var updatedInputMetrics = new ProjectJobScheduleInputMetrics(1, 2, 3, 4, 5)
                .withProjects(10)
                .withJobs(20)
                .withResources(30)
                .withExecutionModes(40)
                .withAllocations(50);

        var updatedOutputMetrics = new ProjectJobScheduleOutputMetrics(1, 2, 3, 4)
                .withTotalMakespan(10)
                .withTotalProjectDelay(20)
                .withTotalScheduledAllocations(30)
                .withTotalUnscheduledAllocations(40);

        assertThat(updatedProject.id()).isEqualTo("p2");
        assertThat(updatedProject.releaseDate()).isEqualTo(4);
        assertThat(updatedProject.criticalPathDuration()).isEqualTo(19);
        assertThat(updatedResource.id()).isEqualTo("r2");
        assertThat(updatedResource.resourceType()).isEqualTo("LOCAL");
        assertThat(updatedResource.capacity()).isEqualTo(20);
        assertThat(updatedResource.renewable()).isFalse();
        assertThat(updatedResource.projectId()).isEqualTo("p2");
        assertThat(updatedJob.id()).isEqualTo("j2");
        assertThat(updatedJob.projectId()).isEqualTo("p2");
        assertThat(updatedJob.jobType()).isEqualTo("SINK");
        assertThat(updatedJob.successorJobIds()).containsExactly("j3");
        assertThat(updatedExecutionMode.id()).isEqualTo("e2");
        assertThat(updatedExecutionMode.jobId()).isEqualTo("j2");
        assertThat(updatedExecutionMode.duration()).isEqualTo(5);
        assertThat(updatedRequirement.id()).isEqualTo("rr2");
        assertThat(updatedRequirement.executionModeId()).isEqualTo("e2");
        assertThat(updatedRequirement.resourceId()).isEqualTo("r2");
        assertThat(updatedRequirement.requirement()).isEqualTo(7);
        assertThat(updatedAllocation.id()).isEqualTo("a2");
        assertThat(updatedAllocation.jobId()).isEqualTo("j2");
        assertThat(updatedAllocation.sourceAllocationId()).isEqualTo("s2");
        assertThat(updatedAllocation.sinkAllocationId()).isEqualTo("k2");
        assertThat(updatedAllocation.predecessorAllocationIds()).containsExactly("a1");
        assertThat(updatedAllocation.successorAllocationIds()).containsExactly("a3");
        assertThat(updatedAllocation.executionModeId()).isEqualTo("e2");
        assertThat(updatedAllocation.delay()).isEqualTo(4);
        assertThat(updatedAllocation.startDate()).isEqualTo(5);
        assertThat(updatedAllocation.endDate()).isEqualTo(6);
        assertThat(updatedJobIdDetail.jobId()).isEqualTo("j2");
        assertThat(updatedResourceIdDetail.resourceId()).isEqualTo("r2");
        assertThat(updatedAllocationIdDetail.allocationId()).isEqualTo("a2");
        assertThat(updatedOverrides.totalProjectDelayWeight()).isEqualTo(10L);
        assertThat(updatedOverrides.totalMakespanWeight()).isEqualTo(20L);
        assertThat(updatedInput.projects()).containsExactly(updatedProject);
        assertThat(updatedInput.resources()).containsExactly(updatedResource);
        assertThat(updatedInput.jobs()).containsExactly(updatedJob);
        assertThat(updatedInput.executionModes()).containsExactly(updatedExecutionMode);
        assertThat(updatedInput.resourceRequirements()).containsExactly(updatedRequirement);
        assertThat(updatedInput.allocations()).containsExactly(updatedAllocation);
        assertThat(updatedOutput.projects()).containsExactly(updatedProject);
        assertThat(updatedOutput.resources()).containsExactly(updatedResource);
        assertThat(updatedOutput.jobs()).containsExactly(updatedJob);
        assertThat(updatedOutput.executionModes()).containsExactly(updatedExecutionMode);
        assertThat(updatedOutput.resourceRequirements()).containsExactly(updatedRequirement);
        assertThat(updatedOutput.allocations()).containsExactly(updatedAllocation);
        assertThat(updatedOutput.score()).isEqualTo("0hard/0medium/1soft");
        assertThat(updatedInputMetrics.projects()).isEqualTo(10);
        assertThat(updatedInputMetrics.jobs()).isEqualTo(20);
        assertThat(updatedInputMetrics.resources()).isEqualTo(30);
        assertThat(updatedInputMetrics.executionModes()).isEqualTo(40);
        assertThat(updatedInputMetrics.allocations()).isEqualTo(50);
        assertThat(updatedOutputMetrics.totalMakespan()).isEqualTo(10);
        assertThat(updatedOutputMetrics.totalProjectDelay()).isEqualTo(20);
        assertThat(updatedOutputMetrics.totalScheduledAllocations()).isEqualTo(30);
        assertThat(updatedOutputMetrics.totalUnscheduledAllocations()).isEqualTo(40);
    }
}
