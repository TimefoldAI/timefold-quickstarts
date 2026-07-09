package org.acme.foodpackaging.demo;

import java.time.Duration;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.data.AbstractBasicDemoDataGenerator;
import ai.timefold.solver.service.definition.api.domain.Configuration;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.domain.ModelRequest;
import ai.timefold.solver.service.definition.api.domain.RunConfiguration;
import ai.timefold.solver.service.definition.api.termination.SolverTerminationConfig;

import org.acme.foodpackaging.dto.PackagingScheduleConfigOverrides;
import org.acme.foodpackaging.dto.PackagingScheduleInput;

@ApplicationScoped
public class DemoDataGenerator
        extends AbstractBasicDemoDataGenerator<PackagingScheduleInput, PackagingScheduleConfigOverrides> {

    @Override
    protected ModelRequest<PackagingScheduleInput, PackagingScheduleConfigOverrides> generateBasicDemoDataRequest() {
        PackagingScheduleInput problem = DemoDataBuilder.builder().build();
        RunConfiguration runConfiguration = new RunConfiguration("BASIC",
                new SolverTerminationConfig(Duration.ofSeconds(30), null));
        // Ship no constraint weight overrides in the demo input, so that any overrides coming from the
        // configuration profile are applied instead of being masked. Callers that want to override
        // specific weights via the input can build a PackagingScheduleConfigOverrides and set only those.
        Configuration<PackagingScheduleConfigOverrides> configuration = new Configuration<>(
                runConfiguration, ModelConfig.empty());
        return new ModelRequest<>(configuration, problem);
    }
}
