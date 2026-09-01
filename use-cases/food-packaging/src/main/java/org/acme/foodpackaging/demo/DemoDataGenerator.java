package org.acme.foodpackaging.demo;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.data.DemoData;
import ai.timefold.solver.service.definition.api.data.DemoMetaData;
import ai.timefold.solver.service.definition.api.domain.Configuration;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.domain.ModelRequest;
import ai.timefold.solver.service.definition.api.domain.RunConfiguration;

import org.acme.foodpackaging.dto.input.PackagingScheduleConfigOverrides;
import org.acme.foodpackaging.dto.input.PackagingScheduleInput;

@ApplicationScoped
public class DemoDataGenerator implements ai.timefold.solver.service.definition.api.data.DemoDataGenerator {

    private static final String BASIC_DEMO_DATA_ID = "BASIC";

    private static final DemoMetaData BASIC_META_DATA = new DemoMetaData(BASIC_DEMO_DATA_ID,
            "Demonstrates a food packaging problem with product-dependent changeover cleaning.",
            "Schedules 100 packaging jobs of 60 vegetable-bag products on 5 production lines run by 5 operators. "
                    + "Switching a line to a product that does not share the previous product's ingredients costs a "
                    + "full cleaning, so the order the jobs end up in decides how much of the day is spent cleaning "
                    + "instead of producing. Once solved, the jobs are sequenced so as many as possible finish "
                    + "before their ideal end time, no job finishes after its maximum end time, and no operator has "
                    + "to clean two of their lines at once.",
            List.of("changeover cleaning", "job sequencing", "due dates", "operator conflicts"),
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
        PackagingScheduleInput problem = DemoDataBuilder.builder().build();

        Configuration<PackagingScheduleConfigOverrides> configuration = new Configuration<>(
                new RunConfiguration(BASIC_DEMO_DATA_ID), ModelConfig.empty());
        return new DemoData(BASIC_META_DATA, new ModelRequest<>(configuration, problem));
    }
}
