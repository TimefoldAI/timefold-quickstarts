package org.acme.meetingschedule.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.acme.meetingschedule.dto.MeetingScheduleInput;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildData() {
        MeetingScheduleInput problem = correctBuilder().build();

        assertEquals(20, problem.people().size());
        assertEquals(3, problem.rooms().size());
        assertFalse(problem.timeGrains().isEmpty());
        assertFalse(problem.meetings().isEmpty());
        assertEquals(problem.meetings().size(), problem.meetingAssignments().size());
        problem.meetings().forEach(meeting -> {
            assertNotNull(meeting.id());
            assertTrue(meeting.durationInGrains() > 0);
        });
        problem.meetingAssignments().forEach(assignment -> {
            assertNotNull(assignment.id());
            assertEquals(null, assignment.startingTimeGrainId());
            assertEquals(null, assignment.roomId());
        });
    }

    @Test
    void personCountGreaterThanZero() {
        DemoDataBuilder builder = DemoDataBuilder.builder().setPersonCount(0).setRandomSeed(0L);
        assertThrows(IllegalStateException.class, builder::build);
    }

    static DemoDataBuilder correctBuilder() {
        return DemoDataBuilder.builder()
                .setPersonCount(20)
                .setRandomSeed(0L);
    }
}
