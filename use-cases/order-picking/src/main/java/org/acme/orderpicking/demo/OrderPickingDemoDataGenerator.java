package org.acme.orderpicking.demo;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.data.DemoData;
import ai.timefold.solver.service.definition.api.data.DemoDataGenerator;
import ai.timefold.solver.service.definition.api.data.DemoMetaData;
import ai.timefold.solver.service.definition.api.domain.Configuration;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.domain.ModelRequest;
import ai.timefold.solver.service.definition.api.domain.RunConfiguration;

import org.acme.orderpicking.dto.input.PickingPlanConfigOverrides;
import org.acme.orderpicking.dto.input.PickingPlanInput;

@ApplicationScoped
public class OrderPickingDemoDataGenerator implements DemoDataGenerator {

    private static final String BASIC = "BASIC";

    private static final DemoMetaData BASIC_META_DATA = new DemoMetaData(BASIC,
            "Demonstrates picking 8 grocery orders with 5 picking carts.",
            """
                    Picks the items of 8 grocery orders, stored on racks grouped by product family, with 5 \
                    pickingCarts of 4 buckets each that all start at the same corner of the warehouse. Once solved, \
                    every picking cart has enough buckets for its orders, walks a short path through the aisles and \
                    orders are split over as few picking carts as possible.""",
            List.of("pickingCarts", "buckets", "orders"));

    @Override
    public List<DemoMetaData> demoMetaData() {
        return List.of(BASIC_META_DATA);
    }

    @Override
    public DemoData generateDemoData(String id) {
        if (!BASIC.equals(id)) {
            throw new IllegalArgumentException("Unknown demo data id (%s).".formatted(id));
        }
        PickingPlanInput problem = DemoDataBuilder.builder().build();
        Configuration<PickingPlanConfigOverrides> configuration = new Configuration<>(new RunConfiguration(id),
                ModelConfig.empty());
        return new DemoData(BASIC_META_DATA, new ModelRequest<>(configuration, problem));
    }
}
