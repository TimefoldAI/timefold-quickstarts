package org.acme.flightcrewscheduling.dto;

import ai.timefold.solver.service.definition.api.ModelConfigOverrides;
import ai.timefold.solver.service.definition.api.domain.ConstraintReference;

import org.acme.flightcrewscheduling.solver.FlightCrewSchedulingConstraintProvider;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "Soft constraint weights. Set a weight to 0 to disable the corresponding constraint. "
        + "A weight left unset (null) is not overridden here, so the value from the configuration profile "
        + "(or the constraint's default) applies. This makes it possible to override some weights via the "
        + "input while leaving others to the configuration profile.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FlightCrewScheduleConfigOverrides(
        @ConstraintReference(FlightCrewSchedulingConstraintProvider.FIRST_ASSIGNMENT_NOT_DEPARTING_FROM_HOME) //
        @Schema(description = "Soft weight of the first assignment not departing from home constraint.") //
        Long firstAssignmentNotDepartingFromHomeWeight,
        @ConstraintReference(FlightCrewSchedulingConstraintProvider.LAST_ASSIGNMENT_NOT_ARRIVING_AT_HOME) //
        @Schema(description = "Soft weight of the last assignment not arriving at home constraint.") //
        Long lastAssignmentNotArrivingAtHomeWeight)
        implements
            ModelConfigOverrides {

    public FlightCrewScheduleConfigOverrides {
        firstAssignmentNotDepartingFromHomeWeight =
                firstAssignmentNotDepartingFromHomeWeight != null && firstAssignmentNotDepartingFromHomeWeight < 0L ? 0L
                        : firstAssignmentNotDepartingFromHomeWeight;
        lastAssignmentNotArrivingAtHomeWeight =
                lastAssignmentNotArrivingAtHomeWeight != null && lastAssignmentNotArrivingAtHomeWeight < 0L ? 0L
                        : lastAssignmentNotArrivingAtHomeWeight;
    }

    public FlightCrewScheduleConfigOverrides() {
        this(1L, 1L);
    }

    public FlightCrewScheduleConfigOverrides withFirstAssignmentNotDepartingFromHomeWeight(
            Long firstAssignmentNotDepartingFromHomeWeight) {
        return new FlightCrewScheduleConfigOverrides(firstAssignmentNotDepartingFromHomeWeight,
                lastAssignmentNotArrivingAtHomeWeight);
    }

    public FlightCrewScheduleConfigOverrides withLastAssignmentNotArrivingAtHomeWeight(
            Long lastAssignmentNotArrivingAtHomeWeight) {
        return new FlightCrewScheduleConfigOverrides(firstAssignmentNotDepartingFromHomeWeight,
                lastAssignmentNotArrivingAtHomeWeight);
    }
}
