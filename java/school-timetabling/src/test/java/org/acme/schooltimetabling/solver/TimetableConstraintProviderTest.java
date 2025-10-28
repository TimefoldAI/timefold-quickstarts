package org.acme.schooltimetabling.solver;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.inject.Inject;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Room;
import org.acme.schooltimetabling.domain.StudentGroup;
import org.acme.schooltimetabling.domain.Teacher;
import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.domain.Timetable;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class TimetableConstraintProviderTest {

    private static final Room ROOM1 = new Room("1", "Room1");
    private static final Room ROOM2 = new Room("2", "Room2");
    private static final Timeslot TIMESLOT1 = new Timeslot("1", DayOfWeek.MONDAY, LocalTime.NOON, LocalTime.NOON.plusHours(1));
    private static final Timeslot TIMESLOT2 = new Timeslot("2", DayOfWeek.TUESDAY, LocalTime.NOON, LocalTime.NOON.plusHours(1));
    private static final Timeslot TIMESLOT3 = new Timeslot("3", DayOfWeek.TUESDAY, LocalTime.NOON.plusHours(1), LocalTime.NOON.plusHours(2));
    private static final Timeslot TIMESLOT4 = new Timeslot("4", DayOfWeek.TUESDAY, LocalTime.NOON.plusHours(3), LocalTime.NOON.plusHours(4));

    private static final StudentGroup STUDENT_GROUP1 = new StudentGroup("1", "Group1");
    private static final StudentGroup STUDENT_GROUP2 = new StudentGroup("2", "Group2");
    private static final StudentGroup STUDENT_GROUP3 = new StudentGroup("3", "Group3");
    private static final StudentGroup STUDENT_GROUP4 = new StudentGroup("4", "Group4");

    // NEW: Teacher constants
    private static final Teacher TEACHER1 = new Teacher("1", "Teacher1");
    private static final Teacher TEACHER2 = new Teacher("2", "Teacher2");
    private static final Teacher TEACHER3 = new Teacher("3", "Teacher3");
    private static final Teacher TEACHER4 = new Teacher("4", "Teacher4");
    private static final Teacher TEACHER5 = new Teacher("5", "Teacher5");

    @Inject
    ConstraintVerifier<TimetableConstraintProvider, Timetable> constraintVerifier;

    @Test
    void roomConflict() {
        // UPDATED: Using Teacher objects
        Lesson firstLesson = new Lesson("1", "Subject1", TEACHER1, STUDENT_GROUP1, TIMESLOT1, ROOM1);
        Lesson conflictingLesson = new Lesson("2", "Subject2", TEACHER2, STUDENT_GROUP2, TIMESLOT1, ROOM1);
        Lesson nonConflictingLesson = new Lesson("3", "Subject3", TEACHER3, STUDENT_GROUP3, TIMESLOT2, ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::roomConflict)
                .given(firstLesson, conflictingLesson, nonConflictingLesson)
                .penalizesBy(1);
    }

    @Test
    void teacherConflict() {
        Teacher conflictingTeacher = TEACHER1;
        // UPDATED: Using Teacher objects
        Lesson firstLesson = new Lesson("1", "Subject1", conflictingTeacher, STUDENT_GROUP1, TIMESLOT1, ROOM1);
        Lesson conflictingLesson = new Lesson("2", "Subject2", conflictingTeacher, STUDENT_GROUP2, TIMESLOT1, ROOM2);
        Lesson nonConflictingLesson = new Lesson("3", "Subject3", TEACHER2, STUDENT_GROUP3, TIMESLOT2, ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::teacherConflict)
                .given(firstLesson, conflictingLesson, nonConflictingLesson)
                .penalizesBy(1);
    }

    @Test
    void studentGroupConflict() {
        StudentGroup conflictingGroup = STUDENT_GROUP1;
        // UPDATED: Using Teacher objects
        Lesson firstLesson = new Lesson("1", "Subject1", TEACHER1, conflictingGroup, TIMESLOT1, ROOM1);
        Lesson conflictingLesson = new Lesson("2", "Subject2", TEACHER2, conflictingGroup, TIMESLOT1, ROOM2);
        Lesson nonConflictingLesson = new Lesson("3", "Subject3", TEACHER3, STUDENT_GROUP3, TIMESLOT2, ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::studentGroupConflict)
                .given(firstLesson, conflictingLesson, nonConflictingLesson)
                .penalizesBy(1);
    }

    @Test
    void teacherRoomStability() {
        Teacher teacher = TEACHER1;
        // UPDATED: Using Teacher objects
        Lesson lessonInFirstRoom = new Lesson("1", "Subject1", teacher, STUDENT_GROUP1, TIMESLOT1, ROOM1);
        Lesson lessonInSameRoom = new Lesson("2", "Subject2", teacher, STUDENT_GROUP2, TIMESLOT2, ROOM1);
        Lesson lessonInDifferentRoom = new Lesson("3", "Subject3", teacher, STUDENT_GROUP3, TIMESLOT3, ROOM2);
        constraintVerifier.verifyThat(TimetableConstraintProvider::teacherRoomStability)
                .given(lessonInFirstRoom, lessonInDifferentRoom, lessonInSameRoom)
                .penalizesBy(1);
    }

    @Test
    void teacherTimeEfficiency() {
        Teacher teacher = TEACHER1;
        // UPDATED: Using Teacher objects
        Lesson singleLessonOnMonday = new Lesson("1", "Subject1", teacher, STUDENT_GROUP1, TIMESLOT1, ROOM1);
        Lesson firstTuesdayLesson = new Lesson("2", "Subject2", teacher, STUDENT_GROUP2, TIMESLOT2, ROOM1);
        Lesson secondTuesdayLesson = new Lesson("3", "Subject3", teacher, STUDENT_GROUP3, TIMESLOT3, ROOM1);
        Lesson thirdTuesdayLessonWithGap = new Lesson("4", "Subject4", teacher, STUDENT_GROUP4, TIMESLOT4, ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::teacherTimeEfficiency)
                .given(singleLessonOnMonday, firstTuesdayLesson, secondTuesdayLesson, thirdTuesdayLessonWithGap)
                .rewardsWith(1);

        // Reverse ID order - UPDATED: Using Teacher objects
        Lesson altSecondTuesdayLesson = new Lesson("2", "Subject2", teacher, STUDENT_GROUP3, TIMESLOT3, ROOM1);
        Lesson altFirstTuesdayLesson = new Lesson("3", "Subject3", teacher, STUDENT_GROUP2, TIMESLOT2, ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::teacherTimeEfficiency)
                .given(altSecondTuesdayLesson, altFirstTuesdayLesson)
                .rewardsWith(1);
    }

    @Test
    void studentGroupSubjectVariety() {
        StudentGroup studentGroup = STUDENT_GROUP1;
        String repeatedSubject = "Subject1";
        // UPDATED: Using Teacher objects
        Lesson mondayLesson = new Lesson("1", repeatedSubject, TEACHER1, studentGroup, TIMESLOT1, ROOM1);
        Lesson firstTuesdayLesson = new Lesson("2", repeatedSubject, TEACHER2, studentGroup, TIMESLOT2, ROOM1);
        Lesson secondTuesdayLesson = new Lesson("3", repeatedSubject, TEACHER3, studentGroup, TIMESLOT3, ROOM1);
        Lesson thirdTuesdayLessonWithDifferentSubject = new Lesson("4", "Subject2", TEACHER4, studentGroup, TIMESLOT4, ROOM1);
        Lesson lessonInAnotherGroup = new Lesson("5", repeatedSubject, TEACHER5, STUDENT_GROUP2, TIMESLOT1, ROOM1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::studentGroupSubjectVariety)
                .given(mondayLesson, firstTuesdayLesson, secondTuesdayLesson, thirdTuesdayLessonWithDifferentSubject,
                        lessonInAnotherGroup)
                .penalizesBy(1);
    }
}