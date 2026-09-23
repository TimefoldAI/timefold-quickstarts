package org.acme.tournamentschedule.demo;

import java.util.List;

import ai.timefold.solver.service.definition.api.data.DemoDataGenerator;
import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.data.DemoData;
import ai.timefold.solver.service.definition.api.data.DemoMetaData;
import ai.timefold.solver.service.definition.api.domain.Configuration;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.domain.ModelRequest;
import ai.timefold.solver.service.definition.api.domain.RunConfiguration;

import org.acme.tournamentschedule.dto.input.TournamentScheduleConfigOverrides;
import org.acme.tournamentschedule.dto.input.TournamentScheduleInput;

@ApplicationScoped
public class TournamentDemoDataGenerator implements DemoDataGenerator {

    private static final String BASIC_DEMO_DATA_ID = "BASIC";

    private static final DemoMetaData BASIC_META_DATA = new DemoMetaData(BASIC_DEMO_DATA_ID,
            "Demonstrates a tournament scheduling problem with unavailability, fairness and balance rules.",
            """
                    Assigns 7 teams to 72 match slots spread across 18 days, 4 matches a day. Some teams are \
                    unavailable on some of those days. Once solved, no team plays twice on the same day, no team \
                    plays on a day it is unavailable, the number of matches is spread as evenly as possible across \
                    all teams, and the number of times each pair of teams confronts each other is as even as \
                    possible.""",
            List.of("fairness", "unavailability", "confrontation balance"),
            List.of());

    @Override
    public List<DemoMetaData> demoMetaData() {
        return List.of(BASIC_META_DATA);
    }

    @Override
    public DemoData generateDemoData(String id) {
        if (!BASIC_DEMO_DATA_ID.equals(id)) {
            throw new IllegalArgumentException("Unknown demo data id (%s).".formatted(id));
        }
        TournamentScheduleInput problem = DemoDataBuilder.basic();
        Configuration<TournamentScheduleConfigOverrides> configuration = new Configuration<>(
                new RunConfiguration(BASIC_DEMO_DATA_ID), ModelConfig.empty());
        return new DemoData(BASIC_META_DATA, new ModelRequest<>(configuration, problem));
    }
}
