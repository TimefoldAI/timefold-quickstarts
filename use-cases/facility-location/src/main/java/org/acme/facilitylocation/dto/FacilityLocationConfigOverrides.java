package org.acme.facilitylocation.dto;

import ai.timefold.solver.service.definition.api.ModelConfigOverrides;
import ai.timefold.solver.service.definition.api.domain.ConstraintReference;

import org.acme.facilitylocation.solver.FacilityLocationConstraintProvider;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "Soft constraint weights. Set a weight to 0 to disable the corresponding constraint. "
        + "A weight left unset (null) is not overridden here, so the value from the configuration profile "
        + "(or the constraint's default) applies. This makes it possible to override some weights via the "
        + "input while leaving others to the configuration profile.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FacilityLocationConfigOverrides(
        @ConstraintReference(FacilityLocationConstraintProvider.FACILITY_SETUP_COST) @Schema(
                description = "Soft weight of the facility setup cost constraint.") Long setupCostWeight,
        @ConstraintReference(FacilityLocationConstraintProvider.DISTANCE_FROM_FACILITY) @Schema(
                description = "Soft weight of the distance from facility constraint.") Long distanceFromFacilityWeight)
        implements
            ModelConfigOverrides {

    public FacilityLocationConfigOverrides {
        setupCostWeight = setupCostWeight != null && setupCostWeight < 0L ? 0L : setupCostWeight;
        distanceFromFacilityWeight =
                distanceFromFacilityWeight != null && distanceFromFacilityWeight < 0L ? 0L : distanceFromFacilityWeight;
    }

    public FacilityLocationConfigOverrides() {
        this(1L, 1L);
    }

    public FacilityLocationConfigOverrides withSetupCostWeight(Long setupCostWeight) {
        return new FacilityLocationConfigOverrides(setupCostWeight, distanceFromFacilityWeight);
    }

    public FacilityLocationConfigOverrides withDistanceFromFacilityWeight(Long distanceFromFacilityWeight) {
        return new FacilityLocationConfigOverrides(setupCostWeight, distanceFromFacilityWeight);
    }
}
