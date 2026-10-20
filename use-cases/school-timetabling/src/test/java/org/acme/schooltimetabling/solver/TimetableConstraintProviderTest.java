package org.acme.schooltimetabling.solver;

import static org.acme.schooltimetabling.support.TestHelper.aLesson;
import static org.acme.schooltimetabling.support.TestHelper.room;
import static org.acme.schooltimetabling.support.TestHelper.timeslot;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Room;
import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.domain.Timetable;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class TimetableConstraintProviderTest {

    private static final Room ROOM1 = room("1");
    private static final Room ROOM2 = room("2");
    private static final Timeslot TIMESLOT1 = timeslot("1", DayOfWeek.MONDAY, LocalTime.NOON);
    private static final Timeslot TIMESLOT2 = timeslot("2", DayOfWeek.TUESDAY, LocalTime.NOON);
    private static final Timeslot TIMESLOT3 = timeslot("3", DayOfWeek.TUESDAY, LocalTime.NOON.plusHours(1));
    private static final Timeslot TIMESLOT4 = timeslot("4", DayOfWeek.TUESDAY, LocalTime.NOON.plusHours(3));

    @Inject
    ConstraintVerifier<TimetableConstraintProvider, Timetable> constraintVerifier;

    @Test
    void roomConflict() {
        Lesson firstLesson = aLesson("1").teacher("Teacher1").studentGroup("Group1").timeslot(TIMESLOT1).room(ROOM1).build();
        Lesson conflictingLesson =
                aLesson("2").teacher("Teacher2").studentGroup("Group2").timeslot(TIMESLOT1).room(ROOM1).build();
        Lesson nonConflictingLesson =
                aLesson("3").teacher("Teacher3").studentGroup("Group3").timeslot(TIMESLOT2).room(ROOM1).build();
        constraintVerifier.verifyThat(TimetableConstraintProvider::roomConflict)
                .given(firstLesson, conflictingLesson, nonConflictingLesson)
                .penalizesBy(1);
    }

    @Test
    void teacherConflict() {
        String conflictingTeacher = "Teacher1";
        Lesson firstLesson =
                aLesson("1").teacher(conflictingTeacher).studentGroup("Group1").timeslot(TIMESLOT1).room(ROOM1).build();
        Lesson conflictingLesson =
                aLesson("2").teacher(conflictingTeacher).studentGroup("Group2").timeslot(TIMESLOT1).room(ROOM2).build();
        Lesson nonConflictingLesson =
                aLesson("3").teacher("Teacher2").studentGroup("Group3").timeslot(TIMESLOT2).room(ROOM1).build();
        constraintVerifier.verifyThat(TimetableConstraintProvider::teacherConflict)
                .given(firstLesson, conflictingLesson, nonConflictingLesson)
                .penalizesBy(1);
    }

    @Test
    void studentGroupConflict() {
        String conflictingGroup = "Group1";
        Lesson firstLesson =
                aLesson("1").teacher("Teacher1").studentGroup(conflictingGroup).timeslot(TIMESLOT1).room(ROOM1).build();
        Lesson conflictingLesson =
                aLesson("2").teacher("Teacher2").studentGroup(conflictingGroup).timeslot(TIMESLOT1).room(ROOM2).build();
        Lesson nonConflictingLesson =
                aLesson("3").teacher("Teacher3").studentGroup("Group3").timeslot(TIMESLOT2).room(ROOM1).build();
        constraintVerifier.verifyThat(TimetableConstraintProvider::studentGroupConflict)
                .given(firstLesson, conflictingLesson, nonConflictingLesson)
                .penalizesBy(1);
    }

    @Test
    void teacherRoomStability() {
        String teacher = "Teacher1";
        Lesson lessonInFirstRoom = aLesson("1").teacher(teacher).studentGroup("Group1").timeslot(TIMESLOT1).room(ROOM1).build();
        Lesson lessonInSameRoom = aLesson("2").teacher(teacher).studentGroup("Group2").timeslot(TIMESLOT1).room(ROOM1).build();
        Lesson lessonInDifferentRoom =
                aLesson("3").teacher(teacher).studentGroup("Group3").timeslot(TIMESLOT1).room(ROOM2).build();
        constraintVerifier.verifyThat(TimetableConstraintProvider::teacherRoomStability)
                .given(lessonInFirstRoom, lessonInDifferentRoom, lessonInSameRoom)
                .penalizesBy(2);
    }

    @Test
    void teacherTimeEfficiency() {
        String teacher = "Teacher1";
        Lesson singleLessonOnMonday =
                aLesson("1").teacher(teacher).studentGroup("Group1").timeslot(TIMESLOT1).room(ROOM1).build();
        Lesson firstTuesdayLesson =
                aLesson("2").teacher(teacher).studentGroup("Group2").timeslot(TIMESLOT2).room(ROOM1).build();
        Lesson secondTuesdayLesson =
                aLesson("3").teacher(teacher).studentGroup("Group3").timeslot(TIMESLOT3).room(ROOM1).build();
        Lesson thirdTuesdayLessonWithGap =
                aLesson("4").teacher(teacher).studentGroup("Group4").timeslot(TIMESLOT4).room(ROOM1).build();
        constraintVerifier.verifyThat(TimetableConstraintProvider::teacherTimeEfficiency)
                .given(singleLessonOnMonday, firstTuesdayLesson, secondTuesdayLesson, thirdTuesdayLessonWithGap)
                .rewardsWith(1); // Second tuesday lesson immediately follows the first.

        // Reverse ID order
        Lesson altSecondTuesdayLesson =
                aLesson("2").teacher(teacher).studentGroup("Group3").timeslot(TIMESLOT3).room(ROOM1).build();
        Lesson altFirstTuesdayLesson =
                aLesson("3").teacher(teacher).studentGroup("Group2").timeslot(TIMESLOT2).room(ROOM1).build();
        constraintVerifier.verifyThat(TimetableConstraintProvider::teacherTimeEfficiency)
                .given(altSecondTuesdayLesson, altFirstTuesdayLesson)
                .rewardsWith(1); // Second tuesday lesson immediately follows the first.
    }

    @Test
    void studentGroupSubjectVariety() {
        String studentGroup = "Group1";
        String repeatedSubject = "Subject1";
        Lesson mondayLesson = aLesson("1").subject(repeatedSubject).teacher("Teacher1").studentGroup(studentGroup)
                .timeslot(TIMESLOT1).room(ROOM1).build();
        Lesson firstTuesdayLesson = aLesson("2").subject(repeatedSubject).teacher("Teacher2").studentGroup(studentGroup)
                .timeslot(TIMESLOT2).room(ROOM1).build();
        Lesson secondTuesdayLesson = aLesson("3").subject(repeatedSubject).teacher("Teacher3").studentGroup(studentGroup)
                .timeslot(TIMESLOT3).room(ROOM1).build();
        Lesson thirdTuesdayLessonWithDifferentSubject = aLesson("4").subject("Subject2").teacher("Teacher4")
                .studentGroup(studentGroup).timeslot(TIMESLOT4).room(ROOM1).build();
        Lesson lessonInAnotherGroup = aLesson("5").subject(repeatedSubject).teacher("Teacher5").studentGroup("Group2")
                .timeslot(TIMESLOT1).room(ROOM1).build();
        constraintVerifier.verifyThat(TimetableConstraintProvider::studentGroupSubjectVariety)
                .given(mondayLesson, firstTuesdayLesson, secondTuesdayLesson, thirdTuesdayLessonWithDifferentSubject,
                        lessonInAnotherGroup)
                .penalizesBy(1); // Second tuesday lesson immediately follows the first.
    }
}
