package org.acme.meetingschedule.demo;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.data.AbstractBasicDemoDataGenerator;
import ai.timefold.solver.service.definition.api.domain.Configuration;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.domain.ModelRequest;
import ai.timefold.solver.service.definition.api.domain.RunConfiguration;

import org.acme.meetingschedule.dto.MeetingScheduleConfigOverrides;
import org.acme.meetingschedule.dto.MeetingScheduleInput;

@ApplicationScoped
public class DemoDataGenerator
        extends
        AbstractBasicDemoDataGenerator<MeetingScheduleInput, MeetingScheduleConfigOverrides> {

    @Override
    protected ModelRequest<MeetingScheduleInput, MeetingScheduleConfigOverrides> generateBasicDemoDataRequest() {
        MeetingScheduleInput problem = DemoDataBuilder.builder()
                .setPersonCount(20)
                .setRandomSeed(0L)
                .build();
        // Ship no constraint weight overrides in the demo input, so that any overrides coming from the
        // configuration profile are applied instead of being masked. Callers that want to override
        // specific weights via the input can build a MeetingScheduleConfigOverrides and set only those.
        Configuration<MeetingScheduleConfigOverrides> configuration = new Configuration<>(
                new RunConfiguration("BASIC"), ModelConfig.empty());
        return new ModelRequest<>(configuration, problem);
    }
}
