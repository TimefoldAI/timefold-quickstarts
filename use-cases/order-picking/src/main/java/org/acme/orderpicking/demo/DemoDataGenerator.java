package org.acme.orderpicking.demo;

import java.time.Duration;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.data.AbstractBasicDemoDataGenerator;
import ai.timefold.solver.service.definition.api.domain.Configuration;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.domain.ModelRequest;
import ai.timefold.solver.service.definition.api.domain.RunConfiguration;
import ai.timefold.solver.service.definition.api.termination.SolverTerminationConfig;

import org.acme.orderpicking.dto.OrderPickingConfigOverrides;
import org.acme.orderpicking.dto.OrderPickingInput;

@ApplicationScoped
public class DemoDataGenerator
        extends
        AbstractBasicDemoDataGenerator<OrderPickingInput, OrderPickingConfigOverrides> {

    private static final int TROLLEYS_COUNT = 5;
    private static final int BUCKET_COUNT = 4;
    private static final int BUCKET_CAPACITY = 60 * 40 * 20;
    private static final int ORDERS_COUNT = 8;
    private static final Duration DEMO_SPENT_LIMIT = Duration.ofSeconds(30);

    @Override
    protected ModelRequest<OrderPickingInput, OrderPickingConfigOverrides> generateBasicDemoDataRequest() {
        OrderPickingInput problem = DemoDataBuilder.builder()
                .setTrolleyCount(TROLLEYS_COUNT)
                .setBucketCount(BUCKET_COUNT)
                .setBucketCapacity(BUCKET_CAPACITY)
                .setOrderCount(ORDERS_COUNT)
                .build();
        SolverTerminationConfig termination = new SolverTerminationConfig(DEMO_SPENT_LIMIT, null);
        RunConfiguration runConfiguration = new RunConfiguration("BASIC", termination);
        // Ship no constraint weight overrides in the demo input, so that any overrides coming from the
        // configuration profile are applied instead of being masked. Callers that want to override
        // specific weights via the input can build an OrderPickingConfigOverrides and set only those.
        ModelConfig<OrderPickingConfigOverrides> modelConfig = ModelConfig.empty();
        Configuration<OrderPickingConfigOverrides> configuration = new Configuration<>(runConfiguration, modelConfig);
        return new ModelRequest<>(configuration, problem);
    }
}
