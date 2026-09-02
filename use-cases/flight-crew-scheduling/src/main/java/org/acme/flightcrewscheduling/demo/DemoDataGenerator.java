package org.acme.flightcrewscheduling.demo;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.data.DemoData;
import ai.timefold.solver.service.definition.api.data.DemoMetaData;
import ai.timefold.solver.service.definition.api.domain.Configuration;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.domain.ModelRequest;
import ai.timefold.solver.service.definition.api.domain.RunConfiguration;

import org.acme.flightcrewscheduling.dto.input.FlightCrewScheduleConfigOverrides;
import org.acme.flightcrewscheduling.dto.input.FlightCrewScheduleInput;

@ApplicationScoped
public class DemoDataGenerator implements ai.timefold.solver.service.definition.api.data.DemoDataGenerator {

    private static final String BASIC_DEMO_DATA_ID = "BASIC";

    private static final DemoMetaData BASIC_META_DATA = new DemoMetaData(BASIC_DEMO_DATA_ID,
            "Demonstrates a flight crew scheduling problem with skills, availability and home bases.",
            "Rosters 44 crew members onto 58 crew seats across 14 flights between 6 airports over one week. "
                    + "The flights form out-and-back rotations from two hubs, London Heathrow and Brussels, and "
                    + "every seat asks for either a pilot or a flight attendant. Once solved, each crew member "
                    + "holds the skill their seat requires, never flies two overlapping flights or flies on a day "
                    + "they are unavailable, only boards a flight departing from where they last landed, and "
                    + "starts and ends their week at their own home airport.",
            List.of("crew skills", "crew availability", "route continuity", "home bases"),
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
        FlightCrewScheduleInput problem = DemoDataBuilder.basic();
        Configuration<FlightCrewScheduleConfigOverrides> configuration = new Configuration<>(
                new RunConfiguration(BASIC_DEMO_DATA_ID), ModelConfig.empty());
        return new DemoData(BASIC_META_DATA, new ModelRequest<>(configuration, problem));
    }
}
