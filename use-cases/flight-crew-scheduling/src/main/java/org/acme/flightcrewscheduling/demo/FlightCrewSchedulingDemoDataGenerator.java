package org.acme.flightcrewscheduling.demo;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.dto.dataset.Configuration;
import ai.timefold.solver.service.definition.api.dto.dataset.ModelConfig;
import ai.timefold.solver.service.definition.api.dto.dataset.ModelRequest;
import ai.timefold.solver.service.definition.api.dto.dataset.RunConfiguration;
import ai.timefold.solver.service.definition.preview.api.demo.DemoData;
import ai.timefold.solver.service.definition.preview.api.demo.DemoDataGenerator;
import ai.timefold.solver.service.definition.preview.api.demo.DemoMetaData;

import org.acme.flightcrewscheduling.dto.input.FlightCrewScheduleConfigOverrides;
import org.acme.flightcrewscheduling.dto.input.FlightCrewScheduleInput;

@ApplicationScoped
public class FlightCrewSchedulingDemoDataGenerator implements DemoDataGenerator {

    private static final String BASIC_DEMO_DATA_ID = "BASIC";

    private static final DemoMetaData BASIC_META_DATA = new DemoMetaData(BASIC_DEMO_DATA_ID,
            "Demonstrates a flight crew scheduling problem with skills, availability and home bases.",
            "Rosters crew members onto crew seats across flights between airports over one week. "
                    + "The flights form out-and-back rotations from two hubs, London Heathrow and Brussels, and "
                    + "every seat asks for either a pilot or a flight attendant. Once solved, each crew member "
                    + "holds the skill their seat requires, never flies two overlapping flights or flies on a day "
                    + "they are unavailable, only boards a flight departing from where they last landed, and "
                    + "starts and ends their week at their own home airport.",
            List.of("crew skills", "crew availability", "route continuity", "home bases"));

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
