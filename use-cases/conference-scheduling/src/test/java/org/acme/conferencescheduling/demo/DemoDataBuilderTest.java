package org.acme.conferencescheduling.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.acme.conferencescheduling.dto.input.ConferenceScheduleInput;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildData() {
        ConferenceScheduleInput problem = DemoDataBuilder.builder().build();

        assertThat(problem.talkTypes()).hasSize(2);
        assertThat(problem.timeslots()).hasSize(6);
        assertThat(problem.rooms()).hasSize(7);
        assertThat(problem.speakers()).hasSize(12);
        assertThat(problem.talks()).hasSize(20);
        problem.talks().forEach(talk -> {
            assertThat(talk.code()).isNotNull();
            assertThat(talk.timeslotId()).isNull();
            assertThat(talk.roomId()).isNull();
        });
    }
}
