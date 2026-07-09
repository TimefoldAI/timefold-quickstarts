package org.acme.maintenancescheduling.demo;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.data.AbstractBasicDemoDataGenerator;
import ai.timefold.solver.service.definition.api.domain.Configuration;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.domain.ModelRequest;
import ai.timefold.solver.service.definition.api.domain.RunConfiguration;

import org.acme.maintenancescheduling.dto.MaintenanceScheduleConfigOverrides;
import org.acme.maintenancescheduling.dto.MaintenanceScheduleInput;

@ApplicationScoped
public class DemoDataGenerator
        extends
        AbstractBasicDemoDataGenerator<MaintenanceScheduleInput, MaintenanceScheduleConfigOverrides> {

    @Override
    protected ModelRequest<MaintenanceScheduleInput, MaintenanceScheduleConfigOverrides> generateBasicDemoDataRequest() {
        MaintenanceScheduleInput problem = DemoDataBuilder.builder()
                .setWeekListSize(8)
                .addCrew("Alpha crew")
                .addCrew("Beta crew")
                .addCrew("Gamma crew")
                .build();
        // Ship no constraint weight overrides in the demo input, so that any overrides coming from the
        // configuration profile are applied instead of being masked. Callers that want to override
        // specific weights via the input can build a MaintenanceScheduleConfigOverrides and set only those.
        Configuration<MaintenanceScheduleConfigOverrides> configuration = new Configuration<>(
                new RunConfiguration("BASIC"), ModelConfig.empty());
        return new ModelRequest<>(configuration, problem);
    }
}
