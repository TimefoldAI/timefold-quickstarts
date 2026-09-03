package org.acme.maintenancescheduling.demo;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.data.DemoData;
import ai.timefold.solver.service.definition.api.data.DemoDataGenerator;
import ai.timefold.solver.service.definition.api.data.DemoMetaData;
import ai.timefold.solver.service.definition.api.domain.Configuration;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.domain.ModelRequest;
import ai.timefold.solver.service.definition.api.domain.RunConfiguration;

import org.acme.maintenancescheduling.dto.input.MaintenanceScheduleConfigOverrides;
import org.acme.maintenancescheduling.dto.input.MaintenanceScheduleInput;

@ApplicationScoped
public class MaintenanceSchedulingDemoDataGenerator implements DemoDataGenerator {

    private static final String BASIC_DEMO_DATA_ID = "BASIC";

    private static final DemoMetaData BASIC_META_DATA = new DemoMetaData(BASIC_DEMO_DATA_ID,
            "Demonstrates a road maintenance scheduling problem with crews, due dates and area conflicts.",
            "Schedules 28 road maintenance jobs onto 4 crews over a 12-week window of workdays. Each job takes "
                    + "1 to 10 workdays, may only start once it is ready, must be finished by its due date, and "
                    + "carries the area it takes place in as a tag. Once solved, every job runs on exactly one "
                    + "crew inside its window, as close as possible to its ideal end date, and jobs in the same "
                    + "area no longer overlap.",
            List.of("crew conflicts", "ready dates", "due dates", "ideal end dates", "area tags"),
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
        MaintenanceScheduleInput problem = DemoDataBuilder.basic();
        Configuration<MaintenanceScheduleConfigOverrides> configuration = new Configuration<>(
                new RunConfiguration(BASIC_DEMO_DATA_ID), ModelConfig.empty());
        return new DemoData(BASIC_META_DATA, new ModelRequest<>(configuration, problem));
    }
}
