package org.acme.projectjobschedule.demo;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.data.AbstractBasicDemoDataGenerator;
import ai.timefold.solver.service.definition.api.domain.Configuration;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.domain.ModelRequest;
import ai.timefold.solver.service.definition.api.domain.RunConfiguration;

import org.acme.projectjobschedule.dto.ProjectJobScheduleConfigOverrides;
import org.acme.projectjobschedule.dto.ProjectJobScheduleInput;

@ApplicationScoped
public class DemoDataGenerator
        extends
        AbstractBasicDemoDataGenerator<ProjectJobScheduleInput, ProjectJobScheduleConfigOverrides> {

    private static final String FIRST_PROJECT = "0";
    private static final String SECOND_PROJECT = "1";

    @Override
    protected ModelRequest<ProjectJobScheduleInput, ProjectJobScheduleConfigOverrides> generateBasicDemoDataRequest() {
        ProjectJobScheduleInput problem = DemoDataBuilder.builder()
                .setJobCount(24)
                .addProject(FIRST_PROJECT, 0, 10)
                .addProject(SECOND_PROJECT, 4, 19)
                .addGlobalResource("0", 16)
                .addLocalResource("1", FIRST_PROJECT, 13, true)
                .addLocalResource("2", FIRST_PROJECT, 44, false)
                .addLocalResource("3", FIRST_PROJECT, 39, false)
                .addLocalResource("4", SECOND_PROJECT, 24, true)
                .addLocalResource("5", SECOND_PROJECT, 66, false)
                .addLocalResource("6", SECOND_PROJECT, 56, false)
                .build();
        // Ship no constraint weight overrides in the demo input, so that any overrides coming from the
        // configuration profile are applied instead of being masked. Callers that want to override
        // specific weights via the input can build a ProjectJobScheduleConfigOverrides and set only those.
        Configuration<ProjectJobScheduleConfigOverrides> configuration = new Configuration<>(
                new RunConfiguration("BASIC"), ModelConfig.empty());
        return new ModelRequest<>(configuration, problem);
    }
}
