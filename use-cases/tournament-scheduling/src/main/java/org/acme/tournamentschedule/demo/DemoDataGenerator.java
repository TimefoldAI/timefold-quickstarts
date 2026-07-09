package org.acme.tournamentschedule.demo;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.data.AbstractBasicDemoDataGenerator;
import ai.timefold.solver.service.definition.api.domain.Configuration;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.domain.ModelRequest;
import ai.timefold.solver.service.definition.api.domain.RunConfiguration;

import org.acme.tournamentschedule.dto.TournamentScheduleConfigOverrides;
import org.acme.tournamentschedule.dto.TournamentScheduleInput;

@ApplicationScoped
public class DemoDataGenerator
        extends
        AbstractBasicDemoDataGenerator<TournamentScheduleInput, TournamentScheduleConfigOverrides> {

    @Override
    protected ModelRequest<TournamentScheduleInput, TournamentScheduleConfigOverrides> generateBasicDemoDataRequest() {
        TournamentScheduleInput problem = DemoDataBuilder.builder()
                .setDayCount(18)
                .setAssignmentsPerDay(4)
                .setUnavailabilityPenaltyCount(12)
                .setRandomSeed(0L)
                .addTeam("Maarten")
                .addTeam("Geoffrey")
                .addTeam("Lukas")
                .addTeam("Chris")
                .addTeam("Fred")
                .addTeam("Radek")
                .addTeam("Maciej")
                .build();
        // Ship no constraint weight overrides in the demo input, so that any overrides coming from the
        // configuration profile are applied instead of being masked. Callers that want to override
        // specific weights via the input can build a TournamentScheduleConfigOverrides and set only those.
        Configuration<TournamentScheduleConfigOverrides> configuration = new Configuration<>(
                new RunConfiguration("BASIC"), ModelConfig.empty());
        return new ModelRequest<>(configuration, problem);
    }
}
