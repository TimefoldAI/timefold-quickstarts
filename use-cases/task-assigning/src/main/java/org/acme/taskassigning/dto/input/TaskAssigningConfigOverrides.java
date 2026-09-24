package org.acme.taskassigning.dto.input;

import ai.timefold.solver.service.definition.api.ModelConfigOverrides;
import ai.timefold.solver.service.definition.api.domain.ConstraintReference;

import org.acme.taskassigning.domain.TaskAssigningConstraintProperties;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "Constraint weights. Set a weight to 0 to disable the corresponding constraint. A weight "
        + "left unset (null) is not overridden here, so the value from the configuration profile (or the "
        + "constraint's default) applies. This makes it possible to override some weights via the input while "
        + "leaving others to the configuration profile.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskAssigningConfigOverrides(
        @ConstraintReference(TaskAssigningConstraintProperties.NO_MISSING_SKILLS) @Schema(
                description = "Hard weight of the noMissingSkills constraint.",
                minimum = "0") Long missingSkillsWeight,
        @ConstraintReference(TaskAssigningConstraintProperties.MINIMIZE_UNASSIGNED_TASKS) @Schema(
                description = "Medium weight of the minimizeUnassignedTasks constraint.",
                minimum = "0") Long unassignedTasksWeight,
        @ConstraintReference(TaskAssigningConstraintProperties.MINIMIZE_MAKESPAN) @Schema(
                description = "Soft weight of the minimizeMakespan constraint.",
                minimum = "0") Long makespanWeight,
        @ConstraintReference(TaskAssigningConstraintProperties.CRITICAL_PRIORITY_TASK_END_TIME) @Schema(
                description = "Soft weight of the criticalPriorityTaskEndTime constraint.",
                minimum = "0") Long criticalPriorityTaskEndTimeWeight,
        @ConstraintReference(TaskAssigningConstraintProperties.MAJOR_PRIORITY_TASK_END_TIME) @Schema(
                description = "Soft weight of the majorPriorityTaskEndTime constraint.",
                minimum = "0") Long majorPriorityTaskEndTimeWeight,
        @ConstraintReference(TaskAssigningConstraintProperties.MINOR_PRIORITY_TASK_END_TIME) @Schema(
                description = "Soft weight of the minorPriorityTaskEndTime constraint.",
                minimum = "0") Long minorPriorityTaskEndTimeWeight)
        implements
            ModelConfigOverrides {

    /**
     * Creates an empty overrides instance: no weight is overridden, so the configuration profile
     * (or each constraint's default) applies. Required by the Service Module to generate the default config profile.
     */
    public TaskAssigningConfigOverrides() {
        this(null, null, null, null, null, null);
    }
}
