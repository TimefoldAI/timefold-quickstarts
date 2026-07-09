package org.acme.schooltimetabling;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.acme.schooltimetabling.dto.LessonDTO;
import org.acme.schooltimetabling.dto.LessonIdDetail;
import org.acme.schooltimetabling.dto.RoomDTO;
import org.acme.schooltimetabling.dto.RoomIdDetail;
import org.acme.schooltimetabling.dto.TimeslotDTO;
import org.acme.schooltimetabling.dto.TimeslotIdDetail;
import org.acme.schooltimetabling.dto.TimetableConfigOverrides;
import org.acme.schooltimetabling.dto.TimetableInput;
import org.acme.schooltimetabling.dto.TimetableInputMetrics;
import org.acme.schooltimetabling.dto.TimetableOutput;
import org.acme.schooltimetabling.dto.TimetableOutputMetrics;
import org.junit.jupiter.api.Test;

class DtoWithMethodsUsageTest {

    @Test
    void allWithMethodsProduceUpdatedCopies() {
        var baseTimeslot = new TimeslotDTO("t1", "MONDAY", "08:30", "09:30");
        var updatedTimeslot = baseTimeslot.withId("t2")
                .withDayOfWeek("TUESDAY")
                .withStartTime("10:00")
                .withEndTime("11:00");

        var baseRoom = new RoomDTO("r1", "Room A");
        var updatedRoom = baseRoom.withId("r2").withName("Room B");

        var baseLesson = new LessonDTO("l1", "Math", "A. Turing", "9th grade", "", "");
        var updatedLesson = baseLesson.withId("l2")
                .withSubject("Physics")
                .withTeacher("M. Curie")
                .withStudentGroup("10th grade")
                .withTimeslotId("t2")
                .withRoomId("r2");

        var updatedLessonIdDetail = new LessonIdDetail("l1").withLessonId("l2");
        var updatedTimeslotIdDetail = new TimeslotIdDetail("t1").withTimeslotId("t2");
        var updatedRoomIdDetail = new RoomIdDetail("r1").withRoomId("r2");

        var updatedOverrides = new TimetableConfigOverrides()
                .withTeacherRoomStabilityWeight(10L)
                .withTeacherTimeEfficiencyWeight(20L)
                .withStudentGroupSubjectVarietyWeight(30L);

        var updatedInput = new TimetableInput(List.of(baseLesson), List.of(baseTimeslot), List.of(baseRoom))
                .withLessons(List.of(updatedLesson))
                .withTimeslots(List.of(updatedTimeslot))
                .withRooms(List.of(updatedRoom));

        var updatedOutput = new TimetableOutput(List.of(baseLesson), List.of(baseTimeslot), List.of(baseRoom), "0hard")
                .withLessons(List.of(updatedLesson))
                .withTimeslots(List.of(updatedTimeslot))
                .withRooms(List.of(updatedRoom))
                .withScore("1hard");

        var updatedInputMetrics = new TimetableInputMetrics(1, 2, 3, 4, 5)
                .withLessons(10)
                .withTimeslots(20)
                .withRooms(30)
                .withTeachers(40)
                .withStudentGroups(50);

        var updatedOutputMetrics = new TimetableOutputMetrics(1, 2, 3, 4)
                .withTotalScheduledLessons(10)
                .withTotalUnscheduledLessons(20)
                .withTotalUsedRooms(30)
                .withTotalUsedTimeslots(40);

        assertThat(updatedTimeslot.id()).isEqualTo("t2");
        assertThat(updatedTimeslot.dayOfWeek()).isEqualTo("TUESDAY");
        assertThat(updatedTimeslot.startTime()).isEqualTo("10:00");
        assertThat(updatedTimeslot.endTime()).isEqualTo("11:00");
        assertThat(updatedRoom.id()).isEqualTo("r2");
        assertThat(updatedRoom.name()).isEqualTo("Room B");
        assertThat(updatedLesson.id()).isEqualTo("l2");
        assertThat(updatedLesson.subject()).isEqualTo("Physics");
        assertThat(updatedLesson.teacher()).isEqualTo("M. Curie");
        assertThat(updatedLesson.studentGroup()).isEqualTo("10th grade");
        assertThat(updatedLesson.timeslotId()).isEqualTo("t2");
        assertThat(updatedLesson.roomId()).isEqualTo("r2");
        assertThat(updatedLessonIdDetail.lessonId()).isEqualTo("l2");
        assertThat(updatedTimeslotIdDetail.timeslotId()).isEqualTo("t2");
        assertThat(updatedRoomIdDetail.roomId()).isEqualTo("r2");
        assertThat(updatedOverrides.teacherRoomStabilityWeight()).isEqualTo(10L);
        assertThat(updatedOverrides.teacherTimeEfficiencyWeight()).isEqualTo(20L);
        assertThat(updatedOverrides.studentGroupSubjectVarietyWeight()).isEqualTo(30L);
        assertThat(updatedInput.lessons()).containsExactly(updatedLesson);
        assertThat(updatedInput.timeslots()).containsExactly(updatedTimeslot);
        assertThat(updatedInput.rooms()).containsExactly(updatedRoom);
        assertThat(updatedOutput.lessons()).containsExactly(updatedLesson);
        assertThat(updatedOutput.timeslots()).containsExactly(updatedTimeslot);
        assertThat(updatedOutput.rooms()).containsExactly(updatedRoom);
        assertThat(updatedOutput.score()).isEqualTo("1hard");
        assertThat(updatedInputMetrics.lessons()).isEqualTo(10);
        assertThat(updatedInputMetrics.timeslots()).isEqualTo(20);
        assertThat(updatedInputMetrics.rooms()).isEqualTo(30);
        assertThat(updatedInputMetrics.teachers()).isEqualTo(40);
        assertThat(updatedInputMetrics.studentGroups()).isEqualTo(50);
        assertThat(updatedOutputMetrics.totalScheduledLessons()).isEqualTo(10);
        assertThat(updatedOutputMetrics.totalUnscheduledLessons()).isEqualTo(20);
        assertThat(updatedOutputMetrics.totalUsedRooms()).isEqualTo(30);
        assertThat(updatedOutputMetrics.totalUsedTimeslots()).isEqualTo(40);
    }
}
