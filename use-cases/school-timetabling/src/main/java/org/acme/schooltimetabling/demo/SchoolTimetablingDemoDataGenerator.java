package org.acme.schooltimetabling.demo;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.data.DemoData;
import ai.timefold.solver.service.definition.api.data.DemoDataGenerator;
import ai.timefold.solver.service.definition.api.data.DemoMetaData;
import ai.timefold.solver.service.definition.api.domain.Configuration;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.domain.ModelRequest;
import ai.timefold.solver.service.definition.api.domain.RunConfiguration;

import org.acme.schooltimetabling.demo.DemoDataBuilder.DemoDataSize;
import org.acme.schooltimetabling.dto.input.TimetableConfigOverrides;
import org.acme.schooltimetabling.dto.input.TimetableInput;

@ApplicationScoped
public class SchoolTimetablingDemoDataGenerator implements DemoDataGenerator {

    private static final DemoMetaData SMALL_META_DATA = new DemoMetaData(DemoDataSize.SMALL.name(),
            "Demonstrates a small school timetable of two student groups over two days.",
            """
                    Schedules 20 lessons of two student groups, taught by five teachers, into ten timeslots on \
                    Monday and Tuesday and three rooms. Once solved, no room, teacher or student group is \
                    double-booked, teachers stay in one room with their lessons close together, and no student \
                    group has the same subject twice in a row.""",
            List.of("rooms", "teachers", "student groups"));

    private static final DemoMetaData LARGE_META_DATA = new DemoMetaData(DemoDataSize.LARGE.name(),
            "Demonstrates a full school week of four student groups.",
            """
                    Schedules 100 lessons of four student groups, taught by seven teachers, into 25 timeslots \
                    from Monday to Friday and six rooms. Once solved, no room, teacher or student group is \
                    double-booked, teachers stay in one room with their lessons close together, and no student \
                    group has the same subject twice in a row.""",
            List.of("rooms", "teachers", "student groups"));

    @Override
    public List<DemoMetaData> demoMetaData() {
        return List.of(SMALL_META_DATA, LARGE_META_DATA);
    }

    @Override
    public DemoData generateDemoData(String id) {
        DemoMetaData metaData = demoMetaData().stream()
                .filter(candidate -> candidate.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown demo data id (%s).".formatted(id)));
        TimetableInput problem = DemoDataBuilder.builder().withSize(DemoDataSize.valueOf(id)).build();
        Configuration<TimetableConfigOverrides> configuration = new Configuration<>(new RunConfiguration(id),
                ModelConfig.empty());
        return new DemoData(metaData, new ModelRequest<>(configuration, problem));
    }
}
