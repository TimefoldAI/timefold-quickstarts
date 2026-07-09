package org.acme.vehiclerouting.dto;

import ai.timefold.solver.service.definition.api.ModelConfigOverrides;
import ai.timefold.solver.service.definition.api.domain.ConstraintReference;

import org.acme.vehiclerouting.solver.VehicleRoutingConstraintProvider;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "Medium and soft constraint weights. Set a weight to 0 to disable the corresponding constraint. "
        + "A weight left unset (null) is not overridden here, so the value from the configuration profile "
        + "(or the constraint's default) applies. This makes it possible to override some weights via the "
        + "input while leaving others to the configuration profile.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VehicleRoutingConfigOverrides(
        @ConstraintReference(VehicleRoutingConstraintProvider.MAXIMIZE_VISITS_ASSIGNED) @Schema(
                description = "Medium weight of the maximize visits assigned constraint.") Long maximizeVisitsAssignedWeight,
        @ConstraintReference(VehicleRoutingConstraintProvider.MINIMIZE_TRAVEL_TIME) @Schema(
                description = "Soft weight of the minimize travel time constraint.") Long minimizeTravelTimeWeight)
        implements
            ModelConfigOverrides {

    public VehicleRoutingConfigOverrides {
        maximizeVisitsAssignedWeight =
                maximizeVisitsAssignedWeight != null && maximizeVisitsAssignedWeight < 0L ? 0L : maximizeVisitsAssignedWeight;
        minimizeTravelTimeWeight =
                minimizeTravelTimeWeight != null && minimizeTravelTimeWeight < 0L ? 0L : minimizeTravelTimeWeight;
    }

    public VehicleRoutingConfigOverrides() {
        this(1L, 1L);
    }

    public VehicleRoutingConfigOverrides withMaximizeVisitsAssignedWeight(Long maximizeVisitsAssignedWeight) {
        return new VehicleRoutingConfigOverrides(maximizeVisitsAssignedWeight, minimizeTravelTimeWeight);
    }

    public VehicleRoutingConfigOverrides withMinimizeTravelTimeWeight(Long minimizeTravelTimeWeight) {
        return new VehicleRoutingConfigOverrides(maximizeVisitsAssignedWeight, minimizeTravelTimeWeight);
    }
}
