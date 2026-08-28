package org.acme.conferencescheduling.demo;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.data.DemoData;
import ai.timefold.solver.service.definition.api.data.DemoMetaData;
import ai.timefold.solver.service.definition.api.domain.Configuration;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.domain.ModelRequest;
import ai.timefold.solver.service.definition.api.domain.RunConfiguration;

import org.acme.conferencescheduling.dto.input.ConferenceScheduleConfigOverrides;
import org.acme.conferencescheduling.dto.input.ConferenceScheduleInput;

@ApplicationScoped
public class DemoDataGenerator implements ai.timefold.solver.service.definition.api.data.DemoDataGenerator {

    private static final String BASIC_DEMO_DATA_ID = "BASIC";

    private static final DemoMetaData BASIC_META_DATA = new DemoMetaData(BASIC_DEMO_DATA_ID,
            "Demonstrates a conference scheduling problem with talk, speaker, room, and audience tags.",
            "Schedules 15 talks by 12 speakers into 6 timeslots across 5 rooms. Talks and speakers carry theme, "
                    + "sector, audience, and content tags that drive requirements, preferences, diversity, and "
                    + "conflict rules. Once solved, talks are placed so speaker and room conflicts are avoided "
                    + "and tag-based preferences (required rooms, preferred timeslots, audience-level flow) are "
                    + "honored.",
            List.of("talk tags", "speaker tags", "room tags", "theme tracks", "audience diversity"),
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
        ConferenceScheduleInput problem = DemoDataBuilder.builder().build();

        Configuration<ConferenceScheduleConfigOverrides> configuration = new Configuration<>(
                new RunConfiguration(BASIC_DEMO_DATA_ID), ModelConfig.empty());
        return new DemoData(BASIC_META_DATA, new ModelRequest<>(configuration, problem));
    }
}
