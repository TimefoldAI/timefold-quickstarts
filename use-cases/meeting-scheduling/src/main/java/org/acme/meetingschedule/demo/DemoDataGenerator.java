package org.acme.meetingschedule.demo;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.data.DemoData;
import ai.timefold.solver.service.definition.api.data.DemoMetaData;
import ai.timefold.solver.service.definition.api.domain.Configuration;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.domain.ModelRequest;
import ai.timefold.solver.service.definition.api.domain.RunConfiguration;

import org.acme.meetingschedule.dto.input.MeetingScheduleConfigOverrides;
import org.acme.meetingschedule.dto.input.MeetingScheduleInput;

@ApplicationScoped
public class DemoDataGenerator implements ai.timefold.solver.service.definition.api.data.DemoDataGenerator {

    private static final String BASIC_DEMO_DATA_ID = "BASIC";

    private static final DemoMetaData BASIC_META_DATA = new DemoMetaData(BASIC_DEMO_DATA_ID,
            "Demonstrates a meeting scheduling problem with room capacity, attendance and office hour rules.",
            "Assigns 24 meetings of two to four hours to 3 rooms across one work week of 08:00 to 18:00 office "
                    + "hours, which a meeting may start anywhere in on a 15 minute grid. Every meeting has a set of "
                    + "required attendees and a set of people who would like to join, drawn from a pool of 20. Once "
                    + "solved, no room is double-booked, no meeting runs past the end of its day, no required "
                    + "attendee is expected in two meetings at once, every room seats everyone attending, and the "
                    + "attendance clashes that are left over are the ones that could not be avoided.",
            List.of("room capacity", "attendance", "office hours", "room stability"),
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
        MeetingScheduleInput problem = DemoDataBuilder.builder().build();
        Configuration<MeetingScheduleConfigOverrides> configuration = new Configuration<>(
                new RunConfiguration(BASIC_DEMO_DATA_ID), ModelConfig.empty());
        return new DemoData(BASIC_META_DATA, new ModelRequest<>(configuration, problem));
    }
}
