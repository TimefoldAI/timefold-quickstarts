package org.acme.flightcrewscheduling.dto.input;

import ai.timefold.solver.service.definition.api.ModelConfigOverrides;
import ai.timefold.solver.service.definition.api.domain.ConstraintReference;

import org.acme.flightcrewscheduling.domain.FlightCrewScheduleConstraintProperties;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "Soft constraint weights. Set a weight to 0 to disable the corresponding constraint. "
        + "A weight left unset (null) is not overridden here, so the value from the configuration profile "
        + "(or the constraint's default) applies. This makes it possible to override some weights via the "
        + "input while leaving others to the configuration profile.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FlightCrewScheduleConfigOverrides(
        @ConstraintReference(FlightCrewScheduleConstraintProperties.FIRST_ASSIGNMENT_NOT_DEPARTING_FROM_HOME) @Schema(
                description = "Soft weight of the firstAssignmentNotDepartingFromHome constraint.",
                minimum = "0") Long firstAssignmentNotDepartingFromHomeWeight,
        @ConstraintReference(FlightCrewScheduleConstraintProperties.LAST_ASSIGNMENT_NOT_ARRIVING_AT_HOME) @Schema(
                description = "Soft weight of the lastAssignmentNotArrivingAtHome constraint.",
                minimum = "0") Long lastAssignmentNotArrivingAtHomeWeight)
        implements
            ModelConfigOverrides {

    /**
     * Creates an empty overrides instance: no weight is overridden, so the configuration profile
     * (or each constraint's default) applies. Required by the Service Module to generate the default config profile.
     */
    public FlightCrewScheduleConfigOverrides() {
        this(null, null);
    }
}
