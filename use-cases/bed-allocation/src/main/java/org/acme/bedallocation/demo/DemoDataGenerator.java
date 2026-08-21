package org.acme.bedallocation.demo;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.data.AbstractBasicDemoDataGenerator;
import ai.timefold.solver.service.definition.api.domain.Configuration;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.domain.ModelRequest;
import ai.timefold.solver.service.definition.api.domain.RunConfiguration;

import org.acme.bedallocation.dto.BedPlanConfigOverrides;
import org.acme.bedallocation.dto.BedPlanInput;

@ApplicationScoped
public class DemoDataGenerator extends AbstractBasicDemoDataGenerator<BedPlanInput, BedPlanConfigOverrides> {

    @Override
    protected ModelRequest<BedPlanInput, BedPlanConfigOverrides> generateBasicDemoDataRequest() {
        BedPlanInput problem = DemoDataBuilder.builder().build();
        // Ship no constraint weight overrides in the demo input, so that any overrides coming from the
        // configuration profile are applied instead of being masked. Callers that want to override
        // specific weights via the input can build a BedPlanConfigOverrides and set only those.
        Configuration<BedPlanConfigOverrides> configuration = new Configuration<>(
                new RunConfiguration("BASIC"), ModelConfig.empty());
        return new ModelRequest<>(configuration, problem);
    }
}
