package org.acme.facilitylocation.demo;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.data.DemoData;
import ai.timefold.solver.service.definition.api.data.DemoMetaData;
import ai.timefold.solver.service.definition.api.domain.Configuration;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.domain.ModelRequest;
import ai.timefold.solver.service.definition.api.domain.RunConfiguration;

import org.acme.facilitylocation.dto.input.FacilityPlanConfigOverrides;
import org.acme.facilitylocation.dto.input.FacilityPlanInput;

@ApplicationScoped
public class FacilityLocationDemoDataGenerator implements ai.timefold.solver.service.definition.api.data.DemoDataGenerator {

    private static final String BASIC_DEMO_DATA_ID = "BASIC";

    private static final DemoMetaData BASIC_META_DATA = new DemoMetaData(BASIC_DEMO_DATA_ID,
            "Demonstrates a facility location problem that trades facility setup cost off against travel distance.",
            """
                    Picks which of 30 candidate facilities to open in north-west London and assigns all 60 consumers to \
                    them. Each facility can serve 150 units of demand and costs around 50 000 to open, while \
                    every consumer demands 15 units, so at least six facilities are needed. Once solved, no \
                    facility is over its capacity and the plan balances the setup cost of the facilities it \
                    opens against the distance its consumers have to travel.""",
            List.of("capacity", "setup cost", "distance"),
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
        FacilityPlanInput problem = DemoDataBuilder.builder().build();
        Configuration<FacilityPlanConfigOverrides> configuration = new Configuration<>(
                new RunConfiguration(BASIC_DEMO_DATA_ID), ModelConfig.empty());
        return new DemoData(BASIC_META_DATA, new ModelRequest<>(configuration, problem));
    }
}
