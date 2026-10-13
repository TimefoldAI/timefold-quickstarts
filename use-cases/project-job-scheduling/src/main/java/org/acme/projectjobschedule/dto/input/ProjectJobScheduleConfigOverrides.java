package org.acme.projectjobschedule.dto.input;

import ai.timefold.solver.service.definition.api.ModelConfigOverrides;
import ai.timefold.solver.service.definition.api.domain.ConstraintReference;

import org.acme.projectjobschedule.domain.ProjectJobScheduleConstraintProperties;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "Weights of the constraints that trade off against each other. Set a weight to 0 to disable "
        + "the corresponding constraint. A weight left unset (null) is not overridden here, so the value from the "
        + "configuration profile (or the constraint's default) applies. This makes it possible to override some "
        + "weights via the input while leaving others to the configuration profile.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProjectJobScheduleConfigOverrides(
        @ConstraintReference(ProjectJobScheduleConstraintProperties.TOTAL_PROJECT_DELAY) @Schema(
                description = "Medium weight of the totalProjectDelay constraint.",
                minimum = "0") Long totalProjectDelayWeight,
        @ConstraintReference(ProjectJobScheduleConstraintProperties.TOTAL_MAKESPAN) @Schema(
                description = "Soft weight of the totalMakespan constraint.", minimum = "0") Long totalMakespanWeight)
        implements
            ModelConfigOverrides {

    /**
     * Creates an empty overrides instance: no weight is overridden, so the configuration profile
     * (or each constraint's default) applies. Required by the Service Module to generate the default config profile.
     */
    public ProjectJobScheduleConfigOverrides() {
        this(null, null);
    }
}
