package org.acme.bedallocation.demo;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.data.AbstractBasicDemoDataGenerator;
import ai.timefold.solver.service.definition.api.domain.Configuration;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.domain.ModelRequest;
import ai.timefold.solver.service.definition.api.domain.RunConfiguration;

import org.acme.bedallocation.dto.input.BedPlanConfigOverrides;
import org.acme.bedallocation.dto.input.BedPlanInput;

// Basic Demo Data Generator, If you want to add more demo datasets, implement DemoDataGenerator directly.
@ApplicationScoped
public class DemoDataGenerator extends AbstractBasicDemoDataGenerator<BedPlanInput, BedPlanConfigOverrides> {

    @Override
    protected ModelRequest<BedPlanInput, BedPlanConfigOverrides> generateBasicDemoDataRequest() {
        BedPlanInput problem = DemoDataBuilder.builder().build();

        Configuration<BedPlanConfigOverrides> configuration = new Configuration<>(
                new RunConfiguration("BASIC"), ModelConfig.empty());
        return new ModelRequest<>(configuration, problem);
    }
}
