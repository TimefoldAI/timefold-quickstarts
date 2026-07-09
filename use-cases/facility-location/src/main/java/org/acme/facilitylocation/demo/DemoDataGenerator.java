package org.acme.facilitylocation.demo;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.data.AbstractBasicDemoDataGenerator;
import ai.timefold.solver.service.definition.api.domain.Configuration;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.domain.ModelRequest;
import ai.timefold.solver.service.definition.api.domain.RunConfiguration;

import org.acme.facilitylocation.dto.FacilityLocationConfigOverrides;
import org.acme.facilitylocation.dto.FacilityLocationInput;
import org.acme.facilitylocation.dto.LocationDTO;

@ApplicationScoped
public class DemoDataGenerator
        extends
        AbstractBasicDemoDataGenerator<FacilityLocationInput, FacilityLocationConfigOverrides> {

    @Override
    protected ModelRequest<FacilityLocationInput, FacilityLocationConfigOverrides> generateBasicDemoDataRequest() {
        FacilityLocationInput problem = DemoDataBuilder.builder().setCapacity(4500).setDemand(900).setFacilityCount(30)
                .setConsumerCount(60).setSouthWestCorner(new LocationDTO(51.44, -0.16))
                .setNorthEastCorner(new LocationDTO(51.56, -0.01)).setAverageSetupCost(50_000)
                .setSetupCostStandardDeviation(10_000).build();
        // Ship no constraint weight overrides in the demo input, so that any overrides coming from the
        // configuration profile are applied instead of being masked. Callers that want to override
        // specific weights via the input can build a FacilityLocationConfigOverrides and set only those.
        Configuration<FacilityLocationConfigOverrides> configuration = new Configuration<>(
                new RunConfiguration("BASIC"), ModelConfig.empty());
        return new ModelRequest<>(configuration, problem);
    }
}
