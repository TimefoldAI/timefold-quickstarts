package org.acme.conferencescheduling.demo;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.data.AbstractBasicDemoDataGenerator;
import ai.timefold.solver.service.definition.api.domain.Configuration;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.domain.ModelRequest;
import ai.timefold.solver.service.definition.api.domain.RunConfiguration;

import org.acme.conferencescheduling.dto.ConferenceScheduleConfigOverrides;
import org.acme.conferencescheduling.dto.ConferenceScheduleInput;

@ApplicationScoped
public class DemoDataGenerator
        extends AbstractBasicDemoDataGenerator<ConferenceScheduleInput, ConferenceScheduleConfigOverrides> {

    @Override
    protected ModelRequest<ConferenceScheduleInput, ConferenceScheduleConfigOverrides> generateBasicDemoDataRequest() {
        ConferenceScheduleInput problem = DemoDataBuilder.builder().build();
        // Ship no constraint weight overrides in the demo input, so that any overrides coming from the
        // configuration profile are applied instead of being masked. Callers that want to override
        // specific weights via the input can build a ConferenceScheduleConfigOverrides and set only those.
        Configuration<ConferenceScheduleConfigOverrides> configuration = new Configuration<>(
                new RunConfiguration("BASIC"), ModelConfig.empty());
        return new ModelRequest<>(configuration, problem);
    }
}
