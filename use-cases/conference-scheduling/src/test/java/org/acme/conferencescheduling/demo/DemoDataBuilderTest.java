package org.acme.conferencescheduling.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.acme.conferencescheduling.dto.ConferenceScheduleInput;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildData() {
        ConferenceScheduleInput problem = DemoDataBuilder.builder().build();

        assertEquals(2, problem.talkTypes().size());
        assertEquals(6, problem.timeslots().size());
        assertEquals(5, problem.rooms().size());
        assertEquals(12, problem.speakers().size());
        assertEquals(15, problem.talks().size());
        problem.talks().forEach(talk -> {
            assertNotNull(talk.code());
            assertEquals(null, talk.timeslotId());
            assertEquals(null, talk.roomId());
        });
    }
}
