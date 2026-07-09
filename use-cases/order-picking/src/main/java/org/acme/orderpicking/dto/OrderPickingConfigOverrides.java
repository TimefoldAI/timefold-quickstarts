package org.acme.orderpicking.dto;

import ai.timefold.solver.service.definition.api.ModelConfigOverrides;
import ai.timefold.solver.service.definition.api.domain.ConstraintReference;

import org.acme.orderpicking.solver.OrderPickingConstraintProvider;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "Soft constraint weights. Set a weight to 0 to disable the corresponding constraint. "
        + "A weight left unset (null) is not overridden here, so the value from the configuration profile "
        + "(or the constraint's default) applies. This makes it possible to override some weights via the "
        + "input while leaving others to the configuration profile.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderPickingConfigOverrides(
        @ConstraintReference(OrderPickingConstraintProvider.MINIMIZE_DISTANCE_FROM_PREVIOUS_PICK) @Schema(
                description = "Soft weight of the minimize distance from previous pick constraint.") Long minimizeDistanceFromPreviousPickWeight,
        @ConstraintReference(OrderPickingConstraintProvider.MINIMIZE_DISTANCE_TO_PATH_ORIGIN) @Schema(
                description = "Soft weight of the minimize distance to the path origin constraint.") Long minimizeDistanceToPathOriginWeight,
        @ConstraintReference(OrderPickingConstraintProvider.MINIMIZE_ORDER_SPLIT_BY_TROLLEY) @Schema(
                description = "Soft weight of the minimize order split by trolley constraint.") Long minimizeOrderSplitByTrolleyWeight)
        implements
            ModelConfigOverrides {

    public OrderPickingConfigOverrides {
        minimizeDistanceFromPreviousPickWeight =
                minimizeDistanceFromPreviousPickWeight != null && minimizeDistanceFromPreviousPickWeight < 0L ? 0L
                        : minimizeDistanceFromPreviousPickWeight;
        minimizeDistanceToPathOriginWeight =
                minimizeDistanceToPathOriginWeight != null && minimizeDistanceToPathOriginWeight < 0L ? 0L
                        : minimizeDistanceToPathOriginWeight;
        minimizeOrderSplitByTrolleyWeight = minimizeOrderSplitByTrolleyWeight != null && minimizeOrderSplitByTrolleyWeight < 0L
                ? 0L
                : minimizeOrderSplitByTrolleyWeight;
    }

    public OrderPickingConfigOverrides() {
        this(1L, 1L, 1L);
    }

    public OrderPickingConfigOverrides withMinimizeDistanceFromPreviousPickWeight(Long minimizeDistanceFromPreviousPickWeight) {
        return new OrderPickingConfigOverrides(minimizeDistanceFromPreviousPickWeight, minimizeDistanceToPathOriginWeight,
                minimizeOrderSplitByTrolleyWeight);
    }

    public OrderPickingConfigOverrides withMinimizeDistanceToPathOriginWeight(Long minimizeDistanceToPathOriginWeight) {
        return new OrderPickingConfigOverrides(minimizeDistanceFromPreviousPickWeight, minimizeDistanceToPathOriginWeight,
                minimizeOrderSplitByTrolleyWeight);
    }

    public OrderPickingConfigOverrides withMinimizeOrderSplitByTrolleyWeight(Long minimizeOrderSplitByTrolleyWeight) {
        return new OrderPickingConfigOverrides(minimizeDistanceFromPreviousPickWeight, minimizeDistanceToPathOriginWeight,
                minimizeOrderSplitByTrolleyWeight);
    }
}
