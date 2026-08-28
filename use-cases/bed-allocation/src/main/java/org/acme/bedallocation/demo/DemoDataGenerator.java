package org.acme.bedallocation.demo;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.data.DemoData;
import ai.timefold.solver.service.definition.api.data.DemoMetaData;
import ai.timefold.solver.service.definition.api.domain.Configuration;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.domain.ModelRequest;
import ai.timefold.solver.service.definition.api.domain.RunConfiguration;

import org.acme.bedallocation.dto.input.BedPlanConfigOverrides;
import org.acme.bedallocation.dto.input.BedPlanInput;

@ApplicationScoped
public class DemoDataGenerator implements ai.timefold.solver.service.definition.api.data.DemoDataGenerator {

    private static final String BASIC_DEMO_DATA_ID = "BASIC";

    private static final DemoMetaData BASIC_META_DATA = new DemoMetaData(BASIC_DEMO_DATA_ID,
            "Demonstrates a hospital bed allocation problem with room capacity, equipment, and gender rules.",
            "Assigns patient stays to beds in a single department of 10 rooms over a 28-day horizon. Stays carry "
                    + "a required specialty and preferred equipment (telemetry, television, oxygen, nitrogen), and "
                    + "rooms enforce gender-mix rules across nights. Once solved, every patient is assigned to a "
                    + "bed that respects gender, age, specialty, and equipment preferences while keeping stays in "
                    + "the same bed night to night.",
            List.of("room capacity", "equipment", "gender rules", "specialty", "age"),
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
        BedPlanInput problem = DemoDataBuilder.builder().build();
        Configuration<BedPlanConfigOverrides> configuration = new Configuration<>(
                new RunConfiguration(BASIC_DEMO_DATA_ID), ModelConfig.empty());
        return new DemoData(BASIC_META_DATA, new ModelRequest<>(configuration, problem));
    }
}
