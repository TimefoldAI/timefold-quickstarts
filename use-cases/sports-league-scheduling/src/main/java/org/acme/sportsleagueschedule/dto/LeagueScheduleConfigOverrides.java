package org.acme.sportsleagueschedule.dto;

import ai.timefold.solver.service.definition.api.ModelConfigOverrides;
import ai.timefold.solver.service.definition.api.domain.ConstraintReference;

import org.acme.sportsleagueschedule.solver.SportsLeagueSchedulingConstraintProvider;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "Soft constraint weights. Set a weight to 0 to disable the corresponding constraint. "
        + "A weight left unset (null) is not overridden here, so the value from the configuration profile "
        + "(or the constraint's default) applies. This makes it possible to override some weights via the "
        + "input while leaving others to the configuration profile.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LeagueScheduleConfigOverrides(
        @ConstraintReference(SportsLeagueSchedulingConstraintProvider.START_TO_AWAY_HOP) @Schema(
                description = "Soft weight of the start to away hop constraint.") Long startToAwayHopWeight,
        @ConstraintReference(SportsLeagueSchedulingConstraintProvider.HOME_TO_AWAY_HOP) @Schema(
                description = "Soft weight of the home to away hop constraint.") Long homeToAwayHopWeight,
        @ConstraintReference(SportsLeagueSchedulingConstraintProvider.AWAY_TO_AWAY_HOP) @Schema(
                description = "Soft weight of the away to away hop constraint.") Long awayToAwayHopWeight,
        @ConstraintReference(SportsLeagueSchedulingConstraintProvider.AWAY_TO_HOME_HOP) @Schema(
                description = "Soft weight of the away to home hop constraint.") Long awayToHomeHopWeight,
        @ConstraintReference(SportsLeagueSchedulingConstraintProvider.AWAY_TO_END_HOP) @Schema(
                description = "Soft weight of the away to end hop constraint.") Long awayToEndHopWeight,
        @ConstraintReference(SportsLeagueSchedulingConstraintProvider.CLASSIC_MATCHES) @Schema(
                description = "Soft weight of the classic matches constraint.") Long classicMatchesWeight)
        implements
            ModelConfigOverrides {

    public LeagueScheduleConfigOverrides {
        startToAwayHopWeight = startToAwayHopWeight != null && startToAwayHopWeight < 0L ? 0L : startToAwayHopWeight;
        homeToAwayHopWeight = homeToAwayHopWeight != null && homeToAwayHopWeight < 0L ? 0L : homeToAwayHopWeight;
        awayToAwayHopWeight = awayToAwayHopWeight != null && awayToAwayHopWeight < 0L ? 0L : awayToAwayHopWeight;
        awayToHomeHopWeight = awayToHomeHopWeight != null && awayToHomeHopWeight < 0L ? 0L : awayToHomeHopWeight;
        awayToEndHopWeight = awayToEndHopWeight != null && awayToEndHopWeight < 0L ? 0L : awayToEndHopWeight;
        classicMatchesWeight = classicMatchesWeight != null && classicMatchesWeight < 0L ? 0L : classicMatchesWeight;
    }

    public LeagueScheduleConfigOverrides() {
        this(1L, 1L, 1L, 1L, 1L, 1000L);
    }

    public LeagueScheduleConfigOverrides withStartToAwayHopWeight(Long startToAwayHopWeight) {
        return new LeagueScheduleConfigOverrides(startToAwayHopWeight, homeToAwayHopWeight, awayToAwayHopWeight,
                awayToHomeHopWeight, awayToEndHopWeight, classicMatchesWeight);
    }

    public LeagueScheduleConfigOverrides withHomeToAwayHopWeight(Long homeToAwayHopWeight) {
        return new LeagueScheduleConfigOverrides(startToAwayHopWeight, homeToAwayHopWeight, awayToAwayHopWeight,
                awayToHomeHopWeight, awayToEndHopWeight, classicMatchesWeight);
    }

    public LeagueScheduleConfigOverrides withAwayToAwayHopWeight(Long awayToAwayHopWeight) {
        return new LeagueScheduleConfigOverrides(startToAwayHopWeight, homeToAwayHopWeight, awayToAwayHopWeight,
                awayToHomeHopWeight, awayToEndHopWeight, classicMatchesWeight);
    }

    public LeagueScheduleConfigOverrides withAwayToHomeHopWeight(Long awayToHomeHopWeight) {
        return new LeagueScheduleConfigOverrides(startToAwayHopWeight, homeToAwayHopWeight, awayToAwayHopWeight,
                awayToHomeHopWeight, awayToEndHopWeight, classicMatchesWeight);
    }

    public LeagueScheduleConfigOverrides withAwayToEndHopWeight(Long awayToEndHopWeight) {
        return new LeagueScheduleConfigOverrides(startToAwayHopWeight, homeToAwayHopWeight, awayToAwayHopWeight,
                awayToHomeHopWeight, awayToEndHopWeight, classicMatchesWeight);
    }

    public LeagueScheduleConfigOverrides withClassicMatchesWeight(Long classicMatchesWeight) {
        return new LeagueScheduleConfigOverrides(startToAwayHopWeight, homeToAwayHopWeight, awayToAwayHopWeight,
                awayToHomeHopWeight, awayToEndHopWeight, classicMatchesWeight);
    }
}
