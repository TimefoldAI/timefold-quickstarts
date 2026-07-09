package org.acme.tournamentschedule.dto;

import ai.timefold.solver.service.definition.api.ModelConfigOverrides;
import ai.timefold.solver.service.definition.api.domain.ConstraintReference;

import org.acme.tournamentschedule.solver.TournamentScheduleConstraintProvider;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "Constraint weights. Set a weight to 0 to disable the corresponding constraint. "
        + "A weight left unset (null) is not overridden here, so the value from the configuration profile "
        + "(or the constraint's default) applies. This makes it possible to override some weights via the "
        + "input while leaving others to the configuration profile.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TournamentScheduleConfigOverrides(
        @ConstraintReference(TournamentScheduleConstraintProvider.FAIR_ASSIGNMENT_COUNT_PER_TEAM) @Schema(
                description = "Weight of the fair assignment count per team constraint.") Long fairAssignmentCountPerTeamWeight,
        @ConstraintReference(TournamentScheduleConstraintProvider.EVENLY_CONFRONTATION_COUNT) @Schema(
                description = "Weight of the evenly confrontation count constraint.") Long evenlyConfrontationCountWeight)
        implements
            ModelConfigOverrides {

    public TournamentScheduleConfigOverrides {
        fairAssignmentCountPerTeamWeight =
                fairAssignmentCountPerTeamWeight != null && fairAssignmentCountPerTeamWeight < 0L ? 0L
                        : fairAssignmentCountPerTeamWeight;
        evenlyConfrontationCountWeight = evenlyConfrontationCountWeight != null && evenlyConfrontationCountWeight < 0L ? 0L
                : evenlyConfrontationCountWeight;
    }

    public TournamentScheduleConfigOverrides() {
        this(1L, 1L);
    }

    public TournamentScheduleConfigOverrides withFairAssignmentCountPerTeamWeight(Long fairAssignmentCountPerTeamWeight) {
        return new TournamentScheduleConfigOverrides(fairAssignmentCountPerTeamWeight, evenlyConfrontationCountWeight);
    }

    public TournamentScheduleConfigOverrides withEvenlyConfrontationCountWeight(Long evenlyConfrontationCountWeight) {
        return new TournamentScheduleConfigOverrides(fairAssignmentCountPerTeamWeight, evenlyConfrontationCountWeight);
    }
}
