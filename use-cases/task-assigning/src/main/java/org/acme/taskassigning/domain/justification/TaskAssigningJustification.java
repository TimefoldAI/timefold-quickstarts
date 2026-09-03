package org.acme.taskassigning.domain.justification;

import ai.timefold.solver.service.definition.api.ModelConstraintJustification;

import org.acme.taskassigning.domain.Employee;
import org.acme.taskassigning.domain.Priority;
import org.acme.taskassigning.domain.Task;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Common contract for every task assigning justification.
 * <p>
 * Each implementation is a record dedicated to exactly one thing that is being justified, so that the Timefold Platform can
 * both render a human-readable {@link #getDescription() description} and expose the individual facts behind it through the
 * OpenAPI schema.
 * <p>
 * Every implementation must be listed in the {@link Schema#oneOf()} below, otherwise it does not show up in the generated
 * OpenAPI schema.
 */
@Schema(description = "Explains why a task assigning constraint was matched.",
        oneOf = {
                // Hard constraints
                TaskAssigningJustification.MissingSkillsJustification.class,

                // Soft constraints
                TaskAssigningJustification.UnassignedTaskJustification.class,
                TaskAssigningJustification.MakespanJustification.class,
                TaskAssigningJustification.PriorityTaskEndTimeJustification.class
        })
public interface TaskAssigningJustification extends ModelConstraintJustification {

    /**
     * @return never null, a human-readable explanation of the constraint match
     */
    String getDescription();

    /**
     * Exposes the description as the {@code description} property of {@link ModelConstraintJustification}.
     */
    default String description() {
        return getDescription();
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    @Schema(description = "An employee is assigned a task that requires skills the employee does not have.",
            allOf = { TaskAssigningJustification.class })
    record MissingSkillsJustification(
            @Schema(description = "The code of the task.") String task,
            @Schema(description = "The full name of the employee assigned to the task.") String employee,
            @Schema(description = "The number of skills the task requires that the employee does not have.") int missingSkillCount)
            implements
                TaskAssigningJustification {

        public static MissingSkillsJustification of(Task task) {
            return new MissingSkillsJustification(task.getCode(), task.getEmployee().getFullName(),
                    task.getMissingSkillCount());
        }

        @Override
        public String getDescription() {
            return "Task '%s' is assigned to '%s', who is missing %d of the required skill(s)."
                    .formatted(task, employee, missingSkillCount);
        }
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    @Schema(description = "A task is not assigned to any employee.", allOf = { TaskAssigningJustification.class })
    record UnassignedTaskJustification(
            @Schema(description = "The code of the unassigned task.") String task)
            implements
                TaskAssigningJustification {

        public static UnassignedTaskJustification of(Task task) {
            return new UnassignedTaskJustification(task.getCode());
        }

        @Override
        public String getDescription() {
            return "Task '%s' is not assigned to any employee.".formatted(task);
        }
    }

    @Schema(description = "An employee's finishing time counts towards the makespan: the time until every "
            + "employee is done.", allOf = { TaskAssigningJustification.class })
    record MakespanJustification(
            @Schema(description = "The full name of the employee.") String employee,
            @Schema(description = "The number of minutes until this employee finishes their last task.") long endTimeInMinutes)
            implements
                TaskAssigningJustification {

        public static MakespanJustification of(Employee employee) {
            return new MakespanJustification(employee.getFullName(), employee.getEndTime());
        }

        @Override
        public String getDescription() {
            return "Employee '%s' does not finish their last task until %d minute(s) in, which counts towards the makespan."
                    .formatted(employee, endTimeInMinutes);
        }
    }

    @Schema(description = "A task finishes later than necessary, relative to its priority.",
            allOf = { TaskAssigningJustification.class })
    record PriorityTaskEndTimeJustification(
            @Schema(description = "The code of the task.") String task,
            @Schema(description = "The priority of the task.") Priority priority,
            @Schema(description = "The number of minutes until the task finishes.") long endTimeInMinutes)
            implements
                TaskAssigningJustification {

        public static PriorityTaskEndTimeJustification of(Task task) {
            return new PriorityTaskEndTimeJustification(task.getCode(), task.getPriority(), task.getEndTime());
        }

        @Override
        public String getDescription() {
            return "%s priority task '%s' does not finish until %d minute(s) in."
                    .formatted(priority, task, endTimeInMinutes);
        }
    }
}
