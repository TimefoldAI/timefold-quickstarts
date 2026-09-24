package org.acme.maintenancescheduling.dto.input;

import ai.timefold.solver.service.definition.api.ModelConfigOverrides;
import ai.timefold.solver.service.definition.api.domain.ConstraintReference;

import org.acme.maintenancescheduling.domain.MaintenanceScheduleConstraintProperties;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "Soft constraint weights. Set a weight to 0 to disable the corresponding constraint. "
        + "A weight left unset (null) is not overridden here, so the value from the configuration profile "
        + "(or the constraint's default) applies. This makes it possible to override some weights via the "
        + "input while leaving others to the configuration profile.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MaintenanceScheduleConfigOverrides(
        @ConstraintReference(MaintenanceScheduleConstraintProperties.BEFORE_IDEAL_END_DATE) @Schema(
                description = "Soft weight of the beforeIdealEndDate constraint.",
                minimum = "0") Long beforeIdealEndDateWeight,
        @ConstraintReference(MaintenanceScheduleConstraintProperties.AFTER_IDEAL_END_DATE) @Schema(
                description = "Soft weight of the afterIdealEndDate constraint.",
                minimum = "0") Long afterIdealEndDateWeight,
        @ConstraintReference(MaintenanceScheduleConstraintProperties.TAG_CONFLICT) @Schema(
                description = "Soft weight of the tagConflict constraint.", minimum = "0") Long tagConflictWeight)
        implements
            ModelConfigOverrides {

    /**
     * Creates an empty overrides instance: no weight is overridden, so the configuration profile
     * (or each constraint's default) applies. Required by the Service Module to generate the default config profile.
     */
    public MaintenanceScheduleConfigOverrides() {
        this(null, null, null);
    }
}
