package org.acme.projectjobschedule.solver;

import static org.acme.projectjobschedule.support.TestHelper.aJob;
import static org.acme.projectjobschedule.support.TestHelper.aProject;
import static org.acme.projectjobschedule.support.TestHelper.aResource;
import static org.acme.projectjobschedule.support.TestHelper.aResourceRequirement;
import static org.acme.projectjobschedule.support.TestHelper.anAllocation;
import static org.acme.projectjobschedule.support.TestHelper.anExecutionMode;

import java.util.List;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.projectjobschedule.domain.Allocation;
import org.acme.projectjobschedule.domain.ExecutionMode;
import org.acme.projectjobschedule.domain.Job;
import org.acme.projectjobschedule.domain.JobType;
import org.acme.projectjobschedule.domain.Project;
import org.acme.projectjobschedule.domain.ProjectJobSchedule;
import org.acme.projectjobschedule.domain.Resource;
import org.acme.projectjobschedule.domain.ResourceRequirement;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class ProjectJobSchedulingConstraintProviderTest {

    @Inject
    ConstraintVerifier<ProjectJobSchedulingConstraintProvider, ProjectJobSchedule> constraintVerifier;

    @Test
    void nonRenewableResourceCapacity() {
        Resource resource = aResource("R1", 5, false);
        ExecutionMode firstMode = anExecutionMode("M1", 2);
        ExecutionMode secondMode = anExecutionMode("M2", 2);
        ResourceRequirement firstRequirement = aResourceRequirement(firstMode, resource, 4);
        ResourceRequirement secondRequirement = aResourceRequirement(secondMode, resource, 4);
        Allocation first = anAllocation("1", aJob("A").executionModes(List.of(firstMode)).build())
                .executionMode(firstMode)
                .build();
        Allocation second = anAllocation("2", aJob("B").executionModes(List.of(secondMode)).build())
                .executionMode(secondMode)
                .build();

        constraintVerifier.verifyThat(ProjectJobSchedulingConstraintProvider::nonRenewableResourceCapacity)
                .given(firstRequirement, secondRequirement, first, second)
                .penalizesBy(3); // the two jobs consume 8 of the capacity of 5, whenever they run

        constraintVerifier.verifyThat(ProjectJobSchedulingConstraintProvider::nonRenewableResourceCapacity)
                .given(firstRequirement, secondRequirement, first)
                .penalizesBy(0); // one job alone stays within the capacity
    }

    @Test
    void renewableResourceCapacity() {
        Resource resource = aResource("R1", 3, true);
        ExecutionMode firstMode = anExecutionMode("M1", 2);
        ExecutionMode secondMode = anExecutionMode("M2", 2);
        ResourceRequirement firstRequirement = aResourceRequirement(firstMode, resource, 2);
        ResourceRequirement secondRequirement = aResourceRequirement(secondMode, resource, 2);
        Allocation first = anAllocation("1", aJob("A").executionModes(List.of(firstMode)).build())
                .executionMode(firstMode)
                .build();
        Allocation overlapping = anAllocation("2", aJob("B").executionModes(List.of(secondMode)).build())
                .executionMode(secondMode)
                .build();

        constraintVerifier.verifyThat(ProjectJobSchedulingConstraintProvider::renewableResourceCapacity)
                .given(firstRequirement, secondRequirement, first, overlapping)
                .penalizesBy(2); // both jobs run on day 0 and 1, consuming 4 of the capacity of 3 on each

        // Delaying the second job past the first one frees the resource again in between.
        Allocation consecutive = anAllocation("2", aJob("B").executionModes(List.of(secondMode)).build())
                .executionMode(secondMode)
                .delay(2)
                .build();

        constraintVerifier.verifyThat(ProjectJobSchedulingConstraintProvider::renewableResourceCapacity)
                .given(firstRequirement, secondRequirement, first, consecutive)
                .penalizesBy(0);
    }

    @Test
    void totalProjectDelay() {
        Project project = aProject("P1", 5);
        ExecutionMode sinkMode = anExecutionMode("sink-1", 0);
        Job sinkJob = aJob("sink").project(project).jobType(JobType.SINK).executionModes(List.of(sinkMode)).build();
        Allocation lateSink = anAllocation("1", sinkJob)
                .executionMode(sinkMode)
                .predecessorsDoneDate(8)
                .build();

        constraintVerifier.verifyThat(ProjectJobSchedulingConstraintProvider::totalProjectDelay)
                .given(lateSink)
                .penalizesBy(3); // the project ends on day 8, three days past its critical path end date of 5

        Allocation onTimeSink = anAllocation("2", sinkJob)
                .executionMode(sinkMode)
                .predecessorsDoneDate(4)
                .build();

        constraintVerifier.verifyThat(ProjectJobSchedulingConstraintProvider::totalProjectDelay)
                .given(onTimeSink)
                .penalizesBy(0); // ending before the critical path end date is not a delay
    }

    @Test
    void totalMakespan() {
        ExecutionMode sinkMode = anExecutionMode("sink-1", 0);
        Job firstSinkJob = aJob("sink-A").project(aProject("A", 5)).jobType(JobType.SINK)
                .executionModes(List.of(sinkMode)).build();
        Job secondSinkJob = aJob("sink-B").project(aProject("B", 5)).jobType(JobType.SINK)
                .executionModes(List.of(sinkMode)).build();
        Allocation first = anAllocation("1", firstSinkJob).executionMode(sinkMode).predecessorsDoneDate(6).build();
        Allocation last = anAllocation("2", secondSinkJob).executionMode(sinkMode).predecessorsDoneDate(9).build();

        constraintVerifier.verifyThat(ProjectJobSchedulingConstraintProvider::totalMakespan)
                .given(first, last)
                .penalizesBy(9); // only the last project ending counts
    }
}
