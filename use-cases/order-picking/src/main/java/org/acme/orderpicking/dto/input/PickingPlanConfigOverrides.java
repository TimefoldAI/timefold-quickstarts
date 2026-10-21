package org.acme.orderpicking.dto.input;

import ai.timefold.solver.service.definition.api.ModelConfigOverrides;
import ai.timefold.solver.service.definition.api.domain.ConstraintReference;

import org.acme.orderpicking.domain.PickingPlanConstraintProperties;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = """
        Soft constraint weights. Set a weight to 0 to disable the corresponding constraint. \
        A weight left unset (null) is not overridden here, so the value from the configuration profile \
        (or the constraint's default) applies. This makes it possible to override some weights via the \
        input while leaving others to the configuration profile.""")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PickingPlanConfigOverrides(
        @ConstraintReference(PickingPlanConstraintProperties.MINIMIZE_DISTANCE_FROM_PREVIOUS_PICK_TASK) @Schema(
                description = "Soft weight of the minimize distance from previous pick task constraint.",
                minimum = "0") Long minimizeDistanceFromPreviousPickTaskWeight,
        @ConstraintReference(PickingPlanConstraintProperties.MINIMIZE_DISTANCE_FROM_LAST_PICK_TASK_TO_PATH_ORIGIN) @Schema(
                description = "Soft weight of the minimize distance from last pick task to path origin constraint.",
                minimum = "0") Long minimizeDistanceFromLastPickTaskToPathOriginWeight,
        @ConstraintReference(PickingPlanConstraintProperties.MINIMIZE_ORDER_SPLIT_BY_PICKING_CART) @Schema(
                description = "Soft weight of the minimize order split by picking cart constraint.",
                minimum = "0") Long minimizeOrderSplitByPickingCartWeight)
        implements
            ModelConfigOverrides {

    /**
     * Creates an empty overrides instance: no weight is overridden, so the configuration profile
     * (or each constraint's default) applies. Required by the Service Module to generate the default config profile.
     */
    public PickingPlanConfigOverrides() {
        this(null, null, null);
    }
}
