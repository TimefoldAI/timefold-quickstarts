package org.acme.sportsleagueschedule.dto.input;

import ai.timefold.solver.service.definition.api.ModelConfigOverrides;
import ai.timefold.solver.service.definition.api.domain.ConstraintReference;

import org.acme.sportsleagueschedule.domain.LeagueScheduleConstraintProperties;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "Soft constraint weights. Set a weight to 0 to disable the corresponding constraint. "
        + "A weight left unset (null) is not overridden here, so the value from the configuration profile "
        + "(or the constraint's default) applies. This makes it possible to override some weights via the "
        + "input while leaving others to the configuration profile.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LeagueScheduleConfigOverrides(
        @ConstraintReference(LeagueScheduleConstraintProperties.START_TO_AWAY_HOP) @Schema(
                description = "Soft weight of the startToAwayHop constraint.",
                minimum = "0") Long startToAwayHopWeight,
        @ConstraintReference(LeagueScheduleConstraintProperties.HOME_TO_AWAY_HOP) @Schema(
                description = "Soft weight of the homeToAwayHop constraint.",
                minimum = "0") Long homeToAwayHopWeight,
        @ConstraintReference(LeagueScheduleConstraintProperties.AWAY_TO_AWAY_HOP) @Schema(
                description = "Soft weight of the awayToAwayHop constraint.",
                minimum = "0") Long awayToAwayHopWeight,
        @ConstraintReference(LeagueScheduleConstraintProperties.AWAY_TO_HOME_HOP) @Schema(
                description = "Soft weight of the awayToHomeHop constraint.",
                minimum = "0") Long awayToHomeHopWeight,
        @ConstraintReference(LeagueScheduleConstraintProperties.AWAY_TO_END_HOP) @Schema(
                description = "Soft weight of the awayToEndHop constraint.",
                minimum = "0") Long awayToEndHopWeight,
        @ConstraintReference(LeagueScheduleConstraintProperties.CLASSIC_MATCHES) @Schema(
                description = "Soft weight of the classicMatches constraint.",
                minimum = "0") Long classicMatchesWeight)
        implements
            ModelConfigOverrides {

    /**
     * Creates an empty overrides instance: no weight is overridden, so the configuration profile
     * (or each constraint's default) applies. Required by the Service Module to generate the default config profile.
     */
    public LeagueScheduleConfigOverrides() {
        this(null, null, null, null, null, null);
    }
}
