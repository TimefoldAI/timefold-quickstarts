package org.acme.schooltimetabling.domain.justification;

import ai.timefold.solver.service.definition.api.ModelConstraintJustification;

import org.acme.schooltimetabling.domain.Lesson;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Common contract for every school timetabling justification.
 * <p>
 * Each implementation is a record dedicated to exactly one thing that is being justified, so that the Timefold Platform can
 * both render a human-readable {@link #getDescription() description} and expose the individual facts behind it through the
 * OpenAPI schema.
 * <p>
 * Every implementation must be listed in the {@link Schema#oneOf()} below, otherwise it does not show up in the generated
 * OpenAPI schema.
 */
@Schema(description = "Explains why a school timetabling constraint was matched.",
        oneOf = {
                // Hard constraints
                TimetableJustification.RoomConflictJustification.class,
                TimetableJustification.TeacherConflictJustification.class,
                TimetableJustification.StudentGroupConflictJustification.class,

                // Soft constraints
                TimetableJustification.TeacherRoomStabilityJustification.class,
                TimetableJustification.TeacherTimeEfficiencyJustification.class,
                TimetableJustification.StudentGroupSubjectVarietyJustification.class
        })
public interface TimetableJustification extends ModelConstraintJustification {

    /**
     * @return never null, a human-readable explanation of the constraint match
     */
    String getDescription();

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    @Schema(description = "Two lessons are scheduled in the same room in the same timeslot.",
            allOf = { TimetableJustification.class })
    record RoomConflictJustification(
            @Schema(description = "The id of the double-booked room.") String room,
            @Schema(description = "The id of the timeslot both lessons are scheduled in.") String timeslot,
            @Schema(description = "The id of the first lesson.") String lesson1,
            @Schema(description = "The id of the second lesson.") String lesson2)
            implements
                TimetableJustification {

        public static RoomConflictJustification of(Lesson lesson1, Lesson lesson2) {
            return new RoomConflictJustification(lesson1.getRoom().id(), lesson1.getTimeslot().id(), lesson1.getId(),
                    lesson2.getId());
        }

        @Override
        public String getDescription() {
            return "Lessons '%s' and '%s' are both scheduled in room '%s' in timeslot '%s'."
                    .formatted(lesson1, lesson2, room, timeslot);
        }
    }

    @Schema(description = "A teacher has two lessons in the same timeslot.",
            allOf = { TimetableJustification.class })
    record TeacherConflictJustification(
            @Schema(description = "The double-booked teacher.") String teacher,
            @Schema(description = "The id of the timeslot both lessons are scheduled in.") String timeslot,
            @Schema(description = "The id of the first lesson.") String lesson1,
            @Schema(description = "The id of the second lesson.") String lesson2)
            implements
                TimetableJustification {

        public static TeacherConflictJustification of(Lesson lesson1, Lesson lesson2) {
            return new TeacherConflictJustification(lesson1.getTeacher(), lesson1.getTimeslot().id(), lesson1.getId(),
                    lesson2.getId());
        }

        @Override
        public String getDescription() {
            return "Teacher '%s' has to teach lessons '%s' and '%s' at the same time, in timeslot '%s'."
                    .formatted(teacher, lesson1, lesson2, timeslot);
        }
    }

    @Schema(description = "A student group has two lessons in the same timeslot.",
            allOf = { TimetableJustification.class })
    record StudentGroupConflictJustification(
            @Schema(description = "The double-booked student group.") String studentGroup,
            @Schema(description = "The id of the timeslot both lessons are scheduled in.") String timeslot,
            @Schema(description = "The id of the first lesson.") String lesson1,
            @Schema(description = "The id of the second lesson.") String lesson2)
            implements
                TimetableJustification {

        public static StudentGroupConflictJustification of(Lesson lesson1, Lesson lesson2) {
            return new StudentGroupConflictJustification(lesson1.getStudentGroup(), lesson1.getTimeslot().id(),
                    lesson1.getId(), lesson2.getId());
        }

        @Override
        public String getDescription() {
            return "Student group '%s' has to attend lessons '%s' and '%s' at the same time, in timeslot '%s'."
                    .formatted(studentGroup, lesson1, lesson2, timeslot);
        }
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    @Schema(description = "A teacher teaches two lessons in different rooms.",
            allOf = { TimetableJustification.class })
    record TeacherRoomStabilityJustification(
            @Schema(description = "The teacher switching rooms.") String teacher,
            @Schema(description = "The id of the first lesson.") String lesson1,
            @Schema(description = "The id of the room the first lesson is taught in.") String room1,
            @Schema(description = "The id of the second lesson.") String lesson2,
            @Schema(description = "The id of the room the second lesson is taught in.") String room2)
            implements
                TimetableJustification {

        public static TeacherRoomStabilityJustification of(Lesson lesson1, Lesson lesson2) {
            return new TeacherRoomStabilityJustification(lesson1.getTeacher(), lesson1.getId(), lesson1.getRoom().id(),
                    lesson2.getId(), lesson2.getRoom().id());
        }

        @Override
        public String getDescription() {
            return "Teacher '%s' teaches lesson '%s' in room '%s', but lesson '%s' in room '%s'."
                    .formatted(teacher, lesson1, room1, lesson2, room2);
        }
    }

    @Schema(description = "A teacher teaches two lessons back to back.",
            allOf = { TimetableJustification.class })
    record TeacherTimeEfficiencyJustification(
            @Schema(description = "The teacher with the consecutive lessons.") String teacher,
            @Schema(description = "The id of the earlier lesson.") String lesson1,
            @Schema(description = "The id of the lesson directly following it.") String lesson2)
            implements
                TimetableJustification {

        public static TeacherTimeEfficiencyJustification of(Lesson lesson1, Lesson lesson2) {
            return new TeacherTimeEfficiencyJustification(lesson1.getTeacher(), lesson1.getId(), lesson2.getId());
        }

        @Override
        public String getDescription() {
            return "Teacher '%s' teaches lesson '%s' directly after lesson '%s'."
                    .formatted(teacher, lesson2, lesson1);
        }
    }

    @Schema(description = "A student group has the same subject in two consecutive lessons.",
            allOf = { TimetableJustification.class })
    record StudentGroupSubjectVarietyJustification(
            @Schema(description = "The student group with the repeated subject.") String studentGroup,
            @Schema(description = "The subject of both lessons.") String subject,
            @Schema(description = "The id of the earlier lesson.") String lesson1,
            @Schema(description = "The id of the lesson directly following it.") String lesson2)
            implements
                TimetableJustification {

        public static StudentGroupSubjectVarietyJustification of(Lesson lesson1, Lesson lesson2) {
            return new StudentGroupSubjectVarietyJustification(lesson1.getStudentGroup(), lesson1.getSubject(),
                    lesson1.getId(), lesson2.getId());
        }

        @Override
        public String getDescription() {
            return "Student group '%s' has %s in lesson '%s' and again directly after, in lesson '%s'."
                    .formatted(studentGroup, subject, lesson1, lesson2);
        }
    }
}
