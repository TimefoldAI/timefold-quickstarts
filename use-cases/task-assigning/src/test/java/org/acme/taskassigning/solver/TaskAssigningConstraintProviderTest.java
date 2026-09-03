package org.acme.taskassigning.solver;

import static org.acme.taskassigning.support.TestHelper.aTask;
import static org.acme.taskassigning.support.TestHelper.aTaskType;
import static org.acme.taskassigning.support.TestHelper.anEmployee;

import java.util.List;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.taskassigning.domain.Priority;
import org.acme.taskassigning.domain.TaskAssigningSolution;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class TaskAssigningConstraintProviderTest {

    @Inject
    ConstraintVerifier<TaskAssigningConstraintProvider, TaskAssigningSolution> constraintVerifier;

    @Test
    void noMissingSkillsUnpenalized() {
        var employee = anEmployee("E1").skills(List.of("Skill A", "Skill B"));
        var task = aTask("1").taskType(aTaskType("T1").requiredSkills(List.of("Skill A", "Skill B")))
                .employee(employee).build();

        constraintVerifier.verifyThat(TaskAssigningConstraintProvider::noMissingSkills)
                .given(task)
                .penalizesBy(0);
    }

    @Test
    void noMissingSkillsPenalized() {
        var employee = anEmployee("E1").skills(List.of("Skill A"));
        var task = aTask("1").taskType(aTaskType("T1").requiredSkills(List.of("Skill A", "Skill B")))
                .employee(employee).build();

        constraintVerifier.verifyThat(TaskAssigningConstraintProvider::noMissingSkills)
                .given(task)
                .penalizesBy(1);
    }

    @Test
    void minimizeUnassignedTasksUnpenalized() {
        var task = aTask("1").employee(anEmployee("E1")).build();

        constraintVerifier.verifyThat(TaskAssigningConstraintProvider::minimizeUnassignedTasks)
                .given(task)
                .penalizesBy(0);
    }

    @Test
    void minimizeUnassignedTasksPenalized() {
        var task = aTask("1").build();

        constraintVerifier.verifyThat(TaskAssigningConstraintProvider::minimizeUnassignedTasks)
                .given(task)
                .penalizesBy(1);
    }

    @Test
    void minimizeMakespanUnpenalized() {
        var employee = anEmployee("E1").build();

        constraintVerifier.verifyThat(TaskAssigningConstraintProvider::minimizeMakespan)
                .given(employee)
                .penalizesBy(0);
    }

    @Test
    void minimizeMakespanPenalized() {
        // Default task type (60 minute base duration) with no employee affinity set (NONE, x4 multiplier),
        // starting at minute 10, so it ends at 10 + 60 * 4 = 250.
        var lastTask = aTask("1").startTime(10);
        var employee = anEmployee("E1").tasks(List.of(lastTask)).build();

        constraintVerifier.verifyThat(TaskAssigningConstraintProvider::minimizeMakespan)
                .given(employee)
                .penalizesBy(250 * 250);
    }

    @Test
    void criticalPriorityTaskEndTimeUnpenalized() {
        // Unassigned, so it does not count towards any priority's end time yet.
        var task = aTask("1").priority(Priority.CRITICAL).build();

        constraintVerifier.verifyThat(TaskAssigningConstraintProvider::criticalPriorityTaskEndTime)
                .given(task)
                .penalizesBy(0);
    }

    @Test
    void criticalPriorityTaskEndTimePenalized() {
        // Ends at 5 + 60 * 4 = 245.
        var task = aTask("1").employee(anEmployee("E1")).priority(Priority.CRITICAL).startTime(5).build();

        constraintVerifier.verifyThat(TaskAssigningConstraintProvider::criticalPriorityTaskEndTime)
                .given(task)
                .penalizesBy(245 * 4);
    }

    @Test
    void majorPriorityTaskEndTimeUnpenalized() {
        var task = aTask("1").priority(Priority.MAJOR).build();

        constraintVerifier.verifyThat(TaskAssigningConstraintProvider::majorPriorityTaskEndTime)
                .given(task)
                .penalizesBy(0);
    }

    @Test
    void majorPriorityTaskEndTimePenalized() {
        var task = aTask("1").employee(anEmployee("E1")).priority(Priority.MAJOR).startTime(5).build();

        constraintVerifier.verifyThat(TaskAssigningConstraintProvider::majorPriorityTaskEndTime)
                .given(task)
                .penalizesBy(245 * 2);
    }

    @Test
    void minorPriorityTaskEndTimeUnpenalized() {
        var task = aTask("1").priority(Priority.MINOR).build();

        constraintVerifier.verifyThat(TaskAssigningConstraintProvider::minorPriorityTaskEndTime)
                .given(task)
                .penalizesBy(0);
    }

    @Test
    void minorPriorityTaskEndTimePenalized() {
        var task = aTask("1").employee(anEmployee("E1")).priority(Priority.MINOR).startTime(5).build();

        constraintVerifier.verifyThat(TaskAssigningConstraintProvider::minorPriorityTaskEndTime)
                .given(task)
                .penalizesBy(245);
    }
}
