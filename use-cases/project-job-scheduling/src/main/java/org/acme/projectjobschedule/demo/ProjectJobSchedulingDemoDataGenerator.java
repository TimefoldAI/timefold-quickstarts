package org.acme.projectjobschedule.demo;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.data.DemoData;
import ai.timefold.solver.service.definition.api.data.DemoDataGenerator;
import ai.timefold.solver.service.definition.api.data.DemoMetaData;
import ai.timefold.solver.service.definition.api.domain.Configuration;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.domain.ModelRequest;
import ai.timefold.solver.service.definition.api.domain.RunConfiguration;

import org.acme.projectjobschedule.dto.input.ProjectJobScheduleConfigOverrides;
import org.acme.projectjobschedule.dto.input.ProjectJobScheduleInput;

@ApplicationScoped
public class ProjectJobSchedulingDemoDataGenerator implements DemoDataGenerator {

    private static final String BASIC_DEMO_DATA_ID = "BASIC";

    private static final DemoMetaData BASIC_META_DATA = new DemoMetaData(BASIC_DEMO_DATA_ID,
            "Demonstrates a project job scheduling problem with execution modes, precedences and resources.",
            """
                    Schedules two projects of twelve jobs each. Every job can be executed in one of three modes, \
                    trading a shorter duration off against the resources it consumes, and can only start once \
                    its project is released and the jobs it depends on have finished. The projects share one \
                    resource and have three of their own; a renewable resource is freed again after every job, a \
                    non-renewable one is consumed for good. Once solved, no resource is overbooked, every project \
                    finishes as close to its critical path as it can, and the overall schedule is as short as \
                    possible.""",
            List.of("execution modes", "job precedences", "renewable resources", "non-renewable resources"));

    @Override
    public List<DemoMetaData> demoMetaData() {
        return List.of(BASIC_META_DATA);
    }

    @Override
    public DemoData generateDemoData(String id) {
        if (!BASIC_DEMO_DATA_ID.equals(id)) {
            throw new IllegalArgumentException("Unknown demo data id (%s).".formatted(id));
        }
        ProjectJobScheduleInput problem = DemoDataBuilder.builder().build();
        Configuration<ProjectJobScheduleConfigOverrides> configuration = new Configuration<>(
                new RunConfiguration(BASIC_DEMO_DATA_ID), ModelConfig.empty());
        return new DemoData(BASIC_META_DATA, new ModelRequest<>(configuration, problem));
    }
}
