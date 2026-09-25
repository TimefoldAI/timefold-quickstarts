package org.acme.facilitylocation.dto.input;

import ai.timefold.solver.service.definition.api.ModelConfigOverrides;
import ai.timefold.solver.service.definition.api.domain.ConstraintReference;

import org.acme.facilitylocation.domain.FacilityPlanConstraintProperties;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "Soft constraint weights. Set a weight to 0 to disable the corresponding constraint. "
        + "A weight left unset (null) is not overridden here, so the value from the configuration profile "
        + "(or the constraint's default) applies. This makes it possible to override some weights via the "
        + "input while leaving others to the configuration profile. Raising the setup cost weight relative to "
        + "the distance weight opens fewer facilities but makes consumers travel further, and vice versa.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FacilityPlanConfigOverrides(
        @ConstraintReference(FacilityPlanConstraintProperties.FACILITY_SETUP_COST) @Schema(
                description = "Soft weight of the facility setup cost constraint.",
                minimum = "0") Long facilitySetupCostWeight,
        @ConstraintReference(FacilityPlanConstraintProperties.DISTANCE_FROM_FACILITY) @Schema(
                description = "Soft weight of the distance from facility constraint.",
                minimum = "0") Long distanceFromFacilityWeight)
        implements
            ModelConfigOverrides {

    /**
     * Creates an empty overrides instance: no weight is overridden, so the configuration profile
     * (or each constraint's default) applies. Required by the Service Module to generate the default config profile.
     */
    public FacilityPlanConfigOverrides() {
        this(null, null);
    }
}
