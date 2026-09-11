package org.acme.foodpackaging.dto.input;

import ai.timefold.solver.service.definition.api.ModelConfigOverrides;
import ai.timefold.solver.service.definition.api.domain.ConstraintReference;

import org.acme.foodpackaging.domain.PackagingScheduleConstraintProperties;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The hard constraints (a job's maximum end time and an operator's cleaning conflicts) are deliberately
 * not overridable here: they express what a schedule has to satisfy to be executable at all, not a
 * trade-off to be tuned per dataset.
 */
@Schema(description = "Weights of the constraints that trade off against each other. Set a weight to 0 to disable the "
        + "corresponding constraint. A weight left unset (null) is not overridden here, so the value from the "
        + "configuration profile (or the constraint's default) applies. This makes it possible to override some "
        + "weights via the input while leaving others to the configuration profile.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PackagingScheduleConfigOverrides(
        @ConstraintReference(PackagingScheduleConstraintProperties.IDEAL_END_DATE_TIME) @Schema(
                description = "Medium weight of the idealEndDateTime constraint.",
                minimum = "0") Long idealEndDateTimeWeight,
        @ConstraintReference(PackagingScheduleConstraintProperties.MAXIMIZE_JOBS_ASSIGNED) @Schema(
                description = "Medium weight of the maximizeJobsAssigned constraint.",
                minimum = "0") Long maximizeJobsAssignedWeight,
        @ConstraintReference(PackagingScheduleConstraintProperties.MINIMIZE_MAKESPAN) @Schema(
                description = "Soft weight of the minimizeMakespan constraint.", minimum = "0") Long minimizeMakespanWeight)
        implements
            ModelConfigOverrides {

    /**
     * Creates an empty overrides instance: no weight is overridden, so the configuration profile
     * (or each constraint's default) applies. Required by the Service Module to generate the default config profile.
     */
    public PackagingScheduleConfigOverrides() {
        this(null, null, null);
    }
}
