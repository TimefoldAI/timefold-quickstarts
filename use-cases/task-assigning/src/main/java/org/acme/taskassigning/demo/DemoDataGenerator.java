package org.acme.taskassigning.demo;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.data.DemoData;
import ai.timefold.solver.service.definition.api.data.DemoMetaData;
import ai.timefold.solver.service.definition.api.domain.Configuration;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.domain.ModelRequest;
import ai.timefold.solver.service.definition.api.domain.RunConfiguration;

import org.acme.taskassigning.dto.input.TaskAssigningConfigOverrides;
import org.acme.taskassigning.dto.input.TaskAssigningInput;

@ApplicationScoped
public class DemoDataGenerator implements ai.timefold.solver.service.definition.api.data.DemoDataGenerator {

    private static final String BASIC_DEMO_DATA_ID = "BASIC";

    private static final DemoMetaData BASIC_META_DATA = new DemoMetaData(BASIC_DEMO_DATA_ID,
            "Demonstrates a task assigning problem with required skills, customer affinities and task priorities.",
            "Assigns 28 tasks of 4 kinds across 4 customers to 8 employees with varying skills and customer "
                    + "affinities. Once solved, every employee is only assigned tasks they have the skills for, "
                    + "as few tasks as possible are left unassigned, the employee who finishes last finishes as "
                    + "early as possible, and higher priority tasks are finished earlier than lower priority ones.",
            List.of("skills", "workload", "priority"),
            List.of());

    @Override
    public List<DemoMetaData> demoMetaData() {
        return List.of(BASIC_META_DATA);
    }

    @Override
    public DemoData generateDemoData(String id) {
        if (!BASIC_DEMO_DATA_ID.equals(id)) {
            throw new IllegalArgumentException("Unknown demo data id (%s).".formatted(id));
        }
        TaskAssigningInput problem = DemoDataBuilder.basic();
        Configuration<TaskAssigningConfigOverrides> configuration = new Configuration<>(
                new RunConfiguration(BASIC_DEMO_DATA_ID), ModelConfig.empty());
        return new DemoData(BASIC_META_DATA, new ModelRequest<>(configuration, problem));
    }
}
