package org.acme.projectjobschedule.solver;

import static org.acme.projectjobschedule.domain.JobType.SINK;
import static org.acme.projectjobschedule.domain.JobType.STANDARD;

import java.util.List;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;

import org.acme.projectjobschedule.domain.Allocation;
import org.acme.projectjobschedule.domain.ExecutionMode;
import org.acme.projectjobschedule.domain.Job;
import org.acme.projectjobschedule.domain.Project;
import org.acme.projectjobschedule.domain.ProjectJobSchedule;
import org.acme.projectjobschedule.domain.ResourceRequirement;
import org.acme.projectjobschedule.domain.resource.LocalResource;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class ProjectJobSchedulingConstraintProviderTest {

    private final ConstraintVerifier<ProjectJobSchedulingConstraintProvider, ProjectJobSchedule> constraintVerifier;

    @Inject
    public ProjectJobSchedulingConstraintProviderTest(
            ConstraintVerifier<ProjectJobSchedulingConstraintProvider, ProjectJobSchedule> constraintVerifier) {
        this.constraintVerifier = constraintVerifier;
    }

    @Test
    void nonRenewableResourceCapacity() {
        Project project = new Project("1");
        LocalResource resource = new LocalResource("1", project, 10, false);

        Job firstJob = new Job("1", project, STANDARD);
        ResourceRequirement firstResourceRequirement = new ResourceRequirement("1", null, resource, 7);
        ExecutionMode firstExecutionMode = new ExecutionMode("1", firstJob, 10, List.of(firstResourceRequirement));
        firstResourceRequirement.setExecutionMode(firstExecutionMode);
        Allocation firstAllocation = new Allocation("1", firstJob);
        firstAllocation.setExecutionMode(firstExecutionMode);
        firstAllocation.setDelay(100);

        Job secondJob = new Job("2", project, STANDARD);
        ResourceRequirement secondResourceRequirement = new ResourceRequirement("2", null, resource, 6);
        ExecutionMode secondExecutionMode = new ExecutionMode("2", secondJob, 10, List.of(secondResourceRequirement));
        secondResourceRequirement.setExecutionMode(secondExecutionMode);
        Allocation secondAllocation = new Allocation("2", secondJob);
        secondAllocation.setExecutionMode(secondExecutionMode);
        secondAllocation.setDelay(100);

        // Cannot use SolutionManager.updateShadows since that would set
        // predecessorDoneDate to 0 (since the two allocations have no predecessors)
        firstAllocation.updateShadowsAfterPredecessorDoneDate();
        secondAllocation.updateShadowsAfterPredecessorDoneDate();

        constraintVerifier.verifyThat(ProjectJobSchedulingConstraintProvider::nonRenewableResourceCapacity)
                .given(firstResourceRequirement, secondResourceRequirement, firstAllocation, secondAllocation)
                .penalizesBy(3);
    }

    @Test
    void renewableResourceCapacity() {
        Project project = new Project("1");
        LocalResource resource = new LocalResource("1", project, 10, true);

        Job firstJob = new Job("1", project, STANDARD);
        ResourceRequirement firstResourceRequirement = new ResourceRequirement("1", null, resource, 7);
        ExecutionMode firstExecutionMode = new ExecutionMode("1", firstJob, 10, List.of(firstResourceRequirement));
        firstResourceRequirement.setExecutionMode(firstExecutionMode);
        Allocation firstAllocation = new Allocation("1", firstJob);
        firstAllocation.setExecutionMode(firstExecutionMode);
        firstAllocation.setDelay(100);
        firstAllocation.setPredecessorsDoneDate(1);

        Job secondJob = new Job("2", project, STANDARD);
        ResourceRequirement secondResourceRequirement = new ResourceRequirement("2", null, resource, 5);
        ExecutionMode secondExecutionMode = new ExecutionMode("2", secondJob, 10, List.of(secondResourceRequirement));
        secondResourceRequirement.setExecutionMode(secondExecutionMode);
        Allocation secondAllocation = new Allocation("2", secondJob);
        secondAllocation.setExecutionMode(secondExecutionMode);
        secondAllocation.setDelay(100);
        secondAllocation.setPredecessorsDoneDate(2);

        // Cannot use SolutionManager.updateShadows since that would set
        // predecessorDoneDate to 0 (since the two allocations have no predecessors)
        firstAllocation.updateShadowsAfterPredecessorDoneDate();
        secondAllocation.updateShadowsAfterPredecessorDoneDate();

        constraintVerifier.verifyThat(ProjectJobSchedulingConstraintProvider::renewableResourceCapacity)
                .given(firstResourceRequirement, secondResourceRequirement, firstAllocation, secondAllocation)
                .penalizesBy(18);
    }

    @Test
    void totalProjectDelay() {
        Project project = new Project("1");
        project.setCriticalPathDuration(100);
        LocalResource resource = new LocalResource("1", project, 10, true);

        Job firstJob = new Job("1", project, SINK);
        ResourceRequirement firstResourceRequirement = new ResourceRequirement("1", null, resource, 7);
        ExecutionMode firstExecutionMode = new ExecutionMode("1", firstJob, 10, List.of(firstResourceRequirement));
        firstResourceRequirement.setExecutionMode(firstExecutionMode);
        Allocation firstAllocation = new Allocation("1", firstJob);
        firstAllocation.setExecutionMode(firstExecutionMode);
        firstAllocation.setDelay(100);
        firstAllocation.setPredecessorsDoneDate(1);

        Job secondJob = new Job("2", project, SINK);
        ResourceRequirement secondResourceRequirement = new ResourceRequirement("2", null, resource, 5);
        ExecutionMode secondExecutionMode = new ExecutionMode("2", secondJob, 10, List.of(secondResourceRequirement));
        secondResourceRequirement.setExecutionMode(secondExecutionMode);
        Allocation secondAllocation = new Allocation("2", secondJob);
        secondAllocation.setExecutionMode(secondExecutionMode);
        secondAllocation.setDelay(100);
        secondAllocation.setPredecessorsDoneDate(2);

        // Cannot use SolutionManager.updateShadows since that would set
        // predecessorDoneDate to 0 (since the two allocations have no predecessors)
        firstAllocation.updateShadowsAfterPredecessorDoneDate();
        secondAllocation.updateShadowsAfterPredecessorDoneDate();

        constraintVerifier.verifyThat(ProjectJobSchedulingConstraintProvider::totalProjectDelay)
                .given(firstAllocation, secondAllocation)
                .penalizesBy(23);
    }

    @Test
    void totalMakespan() {
        Project project = new Project("1");
        project.setCriticalPathDuration(155);
        LocalResource resource = new LocalResource("1", project, 10, true);

        Job firstJob = new Job("1", project, SINK);
        ResourceRequirement firstResourceRequirement = new ResourceRequirement("1", null, resource, 7);
        ExecutionMode firstExecutionMode = new ExecutionMode("1", firstJob, 10, List.of(firstResourceRequirement));
        firstResourceRequirement.setExecutionMode(firstExecutionMode);
        Allocation firstAllocation = new Allocation("1", firstJob);
        firstAllocation.setExecutionMode(firstExecutionMode);
        firstAllocation.setDelay(100);
        firstAllocation.setPredecessorsDoneDate(1);

        Job secondJob = new Job("2", project, SINK);
        ResourceRequirement secondResourceRequirement = new ResourceRequirement("2", null, resource, 5);
        ExecutionMode secondExecutionMode = new ExecutionMode("2", secondJob, 10, List.of(secondResourceRequirement));
        secondResourceRequirement.setExecutionMode(secondExecutionMode);
        Allocation secondAllocation = new Allocation("2", secondJob);
        secondAllocation.setExecutionMode(secondExecutionMode);
        secondAllocation.setDelay(100);
        secondAllocation.setPredecessorsDoneDate(2);

        // Cannot use SolutionManager.updateShadows since that would set
        // predecessorDoneDate to 0 (since the two allocations have no predecessors)
        firstAllocation.updateShadowsAfterPredecessorDoneDate();
        secondAllocation.updateShadowsAfterPredecessorDoneDate();

        constraintVerifier.verifyThat(ProjectJobSchedulingConstraintProvider::totalMakespan)
                .given(firstAllocation, secondAllocation)
                .penalizesBy(112);
    }
}
