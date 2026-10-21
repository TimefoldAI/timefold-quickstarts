package org.acme.employeescheduling.demo;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.data.DemoData;
import ai.timefold.solver.service.definition.api.data.DemoDataGenerator;
import ai.timefold.solver.service.definition.api.data.DemoMetaData;
import ai.timefold.solver.service.definition.api.domain.Configuration;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.domain.ModelRequest;
import ai.timefold.solver.service.definition.api.domain.RunConfiguration;

import org.acme.employeescheduling.demo.DemoDataBuilder.DemoDataSize;
import org.acme.employeescheduling.dto.input.EmployeeScheduleConfigOverrides;
import org.acme.employeescheduling.dto.input.EmployeeScheduleInput;

@ApplicationScoped
public class EmployeeSchedulingDemoDataGenerator implements DemoDataGenerator {

    private static final DemoMetaData BASIC_META_DATA = new DemoMetaData(DemoDataSize.BASIC.name(),
            "Demonstrates a two-week schedule of 15 employees at three hospital locations.",
            """
                    Assigns 15 doctors and nurses to the shifts of three hospital locations over two weeks, starting \
                    on the upcoming Monday. Once solved, every shift is worked by an employee with the required \
                    skill, nobody works on a day they are unavailable, more than one shift a day or without 10 hours \
                    of rest, and the shifts favor the days employees want to work and are spread fairly.""",
            List.of("skills", "availability", "fairness"));

    private static final DemoMetaData LARGE_META_DATA = new DemoMetaData(DemoDataSize.LARGE.name(),
            "Demonstrates a four-week schedule of 50 employees at seven hospital locations.",
            """
                    Assigns 50 doctors and nurses to the shifts of seven hospital locations over four weeks, starting \
                    on the upcoming Monday. Once solved, every shift is worked by an employee with the required \
                    skill, nobody works on a day they are unavailable, more than one shift a day or without 10 hours \
                    of rest, and the shifts favor the days employees want to work and are spread fairly.""",
            List.of("skills", "availability", "fairness"));

    @Override
    public List<DemoMetaData> demoMetaData() {
        return List.of(BASIC_META_DATA, LARGE_META_DATA);
    }

    @Override
    public DemoData generateDemoData(String id) {
        DemoMetaData metaData = demoMetaData().stream()
                .filter(candidate -> candidate.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown demo data id (%s).".formatted(id)));
        EmployeeScheduleInput problem = DemoDataBuilder.builder().withSize(DemoDataSize.valueOf(id)).build();
        Configuration<EmployeeScheduleConfigOverrides> configuration = new Configuration<>(new RunConfiguration(id),
                ModelConfig.empty());
        return new DemoData(metaData, new ModelRequest<>(configuration, problem));
    }
}
