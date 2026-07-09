package org.acme.projectjobschedule.dto;

import ai.timefold.solver.service.definition.api.ModelConfigOverrides;
import ai.timefold.solver.service.definition.api.domain.ConstraintReference;

import org.acme.projectjobschedule.solver.ProjectJobScheduleConstraintProperties;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "Constraint weights. Set a weight to 0 to disable the corresponding constraint. "
        + "A weight left unset (null) is not overridden here, so the value from the configuration profile "
        + "(or the constraint's default) applies. This makes it possible to override some weights via the "
        + "input while leaving others to the configuration profile.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProjectJobScheduleConfigOverrides(
        @ConstraintReference(ProjectJobScheduleConstraintProperties.TOTAL_PROJECT_DELAY) @Schema(
                description = "Medium weight of the total project delay constraint.") Long totalProjectDelayWeight,
        @ConstraintReference(ProjectJobScheduleConstraintProperties.TOTAL_MAKESPAN) @Schema(
                description = "Soft weight of the total makespan constraint.") Long totalMakespanWeight)
        implements
            ModelConfigOverrides {

    public ProjectJobScheduleConfigOverrides {
        totalProjectDelayWeight =
                totalProjectDelayWeight != null && totalProjectDelayWeight < 0L ? 0L : totalProjectDelayWeight;
        totalMakespanWeight = totalMakespanWeight != null && totalMakespanWeight < 0L ? 0L : totalMakespanWeight;
    }

    public ProjectJobScheduleConfigOverrides() {
        this(1L, 1L);
    }

    public ProjectJobScheduleConfigOverrides withTotalProjectDelayWeight(Long totalProjectDelayWeight) {
        return new ProjectJobScheduleConfigOverrides(totalProjectDelayWeight, totalMakespanWeight);
    }

    public ProjectJobScheduleConfigOverrides withTotalMakespanWeight(Long totalMakespanWeight) {
        return new ProjectJobScheduleConfigOverrides(totalProjectDelayWeight, totalMakespanWeight);
    }
}
