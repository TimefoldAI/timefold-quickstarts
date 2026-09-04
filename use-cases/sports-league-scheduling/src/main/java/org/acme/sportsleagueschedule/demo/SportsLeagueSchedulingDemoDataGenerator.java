package org.acme.sportsleagueschedule.demo;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.data.DemoData;
import ai.timefold.solver.service.definition.api.data.DemoDataGenerator;
import ai.timefold.solver.service.definition.api.data.DemoMetaData;
import ai.timefold.solver.service.definition.api.domain.Configuration;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.domain.ModelRequest;
import ai.timefold.solver.service.definition.api.domain.RunConfiguration;

import org.acme.sportsleagueschedule.dto.input.LeagueScheduleConfigOverrides;
import org.acme.sportsleagueschedule.dto.input.LeagueScheduleInput;

@ApplicationScoped
public class SportsLeagueSchedulingDemoDataGenerator implements DemoDataGenerator {

    private static final String BASIC_DEMO_DATA_ID = "BASIC";

    private static final DemoMetaData BASIC_META_DATA = new DemoMetaData(BASIC_DEMO_DATA_ID,
            "Demonstrates a double round-robin league season with travel distances and classic matches.",
            "Schedules 182 matches of a fourteen-team South American league onto 32 matchdays. Every pairing is "
                    + "played twice, once at either venue, and about 5% of them are classics, such as derbies. "
                    + "Once solved, no team plays twice on the same matchday or four matchdays in a row at home "
                    + "or away, no pairing is replayed the very next matchday, the classics land on a weekend, "
                    + "and the kilometres the teams travel between consecutive venues are as few as possible.",
            List.of("schedule conflicts", "team fairness", "travel distance", "match attractiveness"),
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
        LeagueScheduleInput problem = DemoDataBuilder.basic();
        Configuration<LeagueScheduleConfigOverrides> configuration = new Configuration<>(
                new RunConfiguration(BASIC_DEMO_DATA_ID), ModelConfig.empty());
        return new DemoData(BASIC_META_DATA, new ModelRequest<>(configuration, problem));
    }
}
