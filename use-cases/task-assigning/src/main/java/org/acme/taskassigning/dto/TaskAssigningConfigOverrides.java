package org.acme.taskassigning.dto;

import ai.timefold.solver.service.definition.api.ModelConfigOverrides;
import ai.timefold.solver.service.definition.api.domain.ConstraintReference;

import org.acme.taskassigning.solver.TaskAssigningConstraintProvider;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "Soft constraint weights. Set a weight to 0 to disable the corresponding constraint. "
        + "A weight left unset (null) is not overridden here, so the value from the configuration profile "
        + "(or the constraint's default) applies. This makes it possible to override some weights via the "
        + "input while leaving others to the configuration profile.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskAssigningConfigOverrides(
        @ConstraintReference(TaskAssigningConstraintProvider.MINIMIZE_UNASSIGNED_TASKS) @Schema(
                description = "Soft weight of the minimize unassigned tasks constraint.") Long minimizeUnassignedTasksWeight,
        @ConstraintReference(TaskAssigningConstraintProvider.MINIMIZE_MAKESPAN) @Schema(
                description = "Soft weight of the minimize makespan constraint.") Long minimizeMakespanWeight,
        @ConstraintReference(TaskAssigningConstraintProvider.CRITICAL_PRIORITY_TASK_END_TIME) @Schema(
                description = "Soft weight of the critical priority task end time constraint.") Long criticalPriorityWeight,
        @ConstraintReference(TaskAssigningConstraintProvider.MAJOR_PRIORITY_TASK_END_TIME) @Schema(
                description = "Soft weight of the major priority task end time constraint.") Long majorPriorityWeight,
        @ConstraintReference(TaskAssigningConstraintProvider.MINOR_PRIORITY_TASK_END_TIME) @Schema(
                description = "Soft weight of the minor priority task end time constraint.") Long minorPriorityWeight)
        implements
            ModelConfigOverrides {

    public TaskAssigningConfigOverrides {
        minimizeUnassignedTasksWeight =
                minimizeUnassignedTasksWeight != null && minimizeUnassignedTasksWeight < 0L ? 0L
                        : minimizeUnassignedTasksWeight;
        minimizeMakespanWeight =
                minimizeMakespanWeight != null && minimizeMakespanWeight < 0L ? 0L : minimizeMakespanWeight;
        criticalPriorityWeight =
                criticalPriorityWeight != null && criticalPriorityWeight < 0L ? 0L : criticalPriorityWeight;
        majorPriorityWeight = majorPriorityWeight != null && majorPriorityWeight < 0L ? 0L : majorPriorityWeight;
        minorPriorityWeight = minorPriorityWeight != null && minorPriorityWeight < 0L ? 0L : minorPriorityWeight;
    }

    public TaskAssigningConfigOverrides() {
        this(1L, 1L, 1L, 1L, 1L);
    }

    public TaskAssigningConfigOverrides withMinimizeUnassignedTasksWeight(Long minimizeUnassignedTasksWeight) {
        return new TaskAssigningConfigOverrides(minimizeUnassignedTasksWeight, minimizeMakespanWeight, criticalPriorityWeight,
                majorPriorityWeight, minorPriorityWeight);
    }

    public TaskAssigningConfigOverrides withMinimizeMakespanWeight(Long minimizeMakespanWeight) {
        return new TaskAssigningConfigOverrides(minimizeUnassignedTasksWeight, minimizeMakespanWeight, criticalPriorityWeight,
                majorPriorityWeight, minorPriorityWeight);
    }

    public TaskAssigningConfigOverrides withCriticalPriorityWeight(Long criticalPriorityWeight) {
        return new TaskAssigningConfigOverrides(minimizeUnassignedTasksWeight, minimizeMakespanWeight, criticalPriorityWeight,
                majorPriorityWeight, minorPriorityWeight);
    }

    public TaskAssigningConfigOverrides withMajorPriorityWeight(Long majorPriorityWeight) {
        return new TaskAssigningConfigOverrides(minimizeUnassignedTasksWeight, minimizeMakespanWeight, criticalPriorityWeight,
                majorPriorityWeight, minorPriorityWeight);
    }

    public TaskAssigningConfigOverrides withMinorPriorityWeight(Long minorPriorityWeight) {
        return new TaskAssigningConfigOverrides(minimizeUnassignedTasksWeight, minimizeMakespanWeight, criticalPriorityWeight,
                majorPriorityWeight, minorPriorityWeight);
    }
}
