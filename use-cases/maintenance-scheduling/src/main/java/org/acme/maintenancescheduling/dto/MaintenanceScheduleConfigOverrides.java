package org.acme.maintenancescheduling.dto;

import ai.timefold.solver.service.definition.api.ModelConfigOverrides;
import ai.timefold.solver.service.definition.api.domain.ConstraintReference;

import org.acme.maintenancescheduling.solver.MaintenanceScheduleConstraintProvider;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "Soft constraint weights. Set a weight to 0 to disable the corresponding constraint. "
        + "A weight left unset (null) is not overridden here, so the value from the configuration profile "
        + "(or the constraint's default) applies. This makes it possible to override some weights via the "
        + "input while leaving others to the configuration profile.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MaintenanceScheduleConfigOverrides(
        @ConstraintReference(MaintenanceScheduleConstraintProvider.BEFORE_IDEAL_END_DATE) @Schema(
                description = "Soft weight of the before ideal end date constraint.") Long beforeIdealEndDateWeight,
        @ConstraintReference(MaintenanceScheduleConstraintProvider.AFTER_IDEAL_END_DATE) @Schema(
                description = "Soft weight of the after ideal end date constraint.") Long afterIdealEndDateWeight,
        @ConstraintReference(MaintenanceScheduleConstraintProvider.TAG_CONFLICT) @Schema(
                description = "Soft weight of the tag conflict constraint.") Long tagConflictWeight)
        implements
            ModelConfigOverrides {

    public MaintenanceScheduleConfigOverrides {
        beforeIdealEndDateWeight = beforeIdealEndDateWeight != null && beforeIdealEndDateWeight < 0L ? 0L
                : beforeIdealEndDateWeight;
        afterIdealEndDateWeight = afterIdealEndDateWeight != null && afterIdealEndDateWeight < 0L ? 0L
                : afterIdealEndDateWeight;
        tagConflictWeight = tagConflictWeight != null && tagConflictWeight < 0L ? 0L : tagConflictWeight;
    }

    public MaintenanceScheduleConfigOverrides() {
        this(1L, 1_000_000L, 1_000L);
    }

    public MaintenanceScheduleConfigOverrides withBeforeIdealEndDateWeight(Long beforeIdealEndDateWeight) {
        return new MaintenanceScheduleConfigOverrides(beforeIdealEndDateWeight, afterIdealEndDateWeight, tagConflictWeight);
    }

    public MaintenanceScheduleConfigOverrides withAfterIdealEndDateWeight(Long afterIdealEndDateWeight) {
        return new MaintenanceScheduleConfigOverrides(beforeIdealEndDateWeight, afterIdealEndDateWeight, tagConflictWeight);
    }

    public MaintenanceScheduleConfigOverrides withTagConflictWeight(Long tagConflictWeight) {
        return new MaintenanceScheduleConfigOverrides(beforeIdealEndDateWeight, afterIdealEndDateWeight, tagConflictWeight);
    }
}
