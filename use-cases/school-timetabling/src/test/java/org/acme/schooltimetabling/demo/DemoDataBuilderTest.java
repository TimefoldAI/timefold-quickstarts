package org.acme.schooltimetabling.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;

import org.acme.schooltimetabling.demo.DemoDataBuilder.DemoDataSize;
import org.acme.schooltimetabling.dto.input.LessonInputDTO;
import org.acme.schooltimetabling.dto.input.TimeslotInputDTO;
import org.acme.schooltimetabling.dto.input.TimetableInput;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildSmallData() {
        TimetableInput problem = DemoDataBuilder.builder().withSize(DemoDataSize.SMALL).build();

        assertThat(problem.timeslots()).hasSize(10);
        assertThat(problem.timeslots()).extracting(TimeslotInputDTO::dayOfWeek)
                .containsOnly(DayOfWeek.MONDAY, DayOfWeek.TUESDAY);
        assertThat(problem.rooms()).hasSize(3);
        assertThat(problem.lessons()).hasSize(20);
        assertThat(problem.lessons().stream().map(LessonInputDTO::teacher).distinct()).hasSize(5);
        assertThat(problem.lessons()).extracting(LessonInputDTO::studentGroup)
                .containsOnly("9th grade", "10th grade");
        assertUnassignedWithUniqueIds(problem);
    }

    @Test
    void shouldBuildLargeData() {
        TimetableInput problem = DemoDataBuilder.builder().withSize(DemoDataSize.LARGE).build();

        assertThat(problem.timeslots()).hasSize(25);
        assertThat(problem.timeslots()).extracting(TimeslotInputDTO::dayOfWeek).containsOnly(DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
        assertThat(problem.rooms()).hasSize(6);
        assertThat(problem.lessons()).hasSize(100);
        assertThat(problem.lessons().stream().map(LessonInputDTO::teacher).distinct()).hasSize(7);
        assertThat(problem.lessons()).extracting(LessonInputDTO::studentGroup)
                .containsOnly("9th grade", "10th grade", "11th grade", "12th grade");
        assertUnassignedWithUniqueIds(problem);
    }

    @Test
    void shouldBeReproducible() {
        assertThat(DemoDataBuilder.builder().withSize(DemoDataSize.LARGE).build())
                .isEqualTo(DemoDataBuilder.builder().withSize(DemoDataSize.LARGE).build());
    }

    private static void assertUnassignedWithUniqueIds(TimetableInput problem) {
        problem.timeslots().forEach(timeslot -> assertThat(timeslot.endTime()).isAfter(timeslot.startTime()));
        assertThat(problem.timeslots()).extracting(TimeslotInputDTO::id).doesNotHaveDuplicates();
        assertThat(problem.lessons()).extracting(LessonInputDTO::id).doesNotHaveDuplicates();
        assertThat(problem.lessons()).allSatisfy(lesson -> {
            assertThat(lesson.timeslotId()).isNull();
            assertThat(lesson.roomId()).isNull();
        });
    }
}
