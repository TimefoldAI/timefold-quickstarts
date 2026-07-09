package org.acme.schooltimetabling.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.acme.schooltimetabling.dto.TimetableInput;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildData() {
        TimetableInput problem = correctBuilder().build();

        assertEquals(10, problem.timeslots().size());
        assertEquals(3, problem.rooms().size());
        assertEquals(2, problem.lessons().size());
        problem.lessons().forEach(lesson -> {
            assertNotNull(lesson.id());
            assertEquals(null, lesson.timeslotId());
            assertEquals(null, lesson.roomId());
        });
    }

    @Test
    void dayCountGreaterThanZero() {
        DemoDataBuilder builder = correctBuilder().setDayCount(0);
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void roomCountGreaterThanZero() {
        DemoDataBuilder builder = correctBuilder().setRoomCount(0);
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void atLeastOneLesson() {
        DemoDataBuilder builder = DemoDataBuilder.builder().setDayCount(2).setRoomCount(3);
        assertThrows(IllegalStateException.class, builder::build);
    }

    static DemoDataBuilder correctBuilder() {
        return DemoDataBuilder.builder()
                .setDayCount(2)
                .setRoomCount(3)
                .addLesson("Math", "A. Turing", "9th grade")
                .addLesson("Physics", "M. Curie", "9th grade");
    }
}
