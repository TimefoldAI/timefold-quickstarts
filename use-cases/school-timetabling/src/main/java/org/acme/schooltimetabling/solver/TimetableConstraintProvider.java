package org.acme.schooltimetabling.solver;

import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import ai.timefold.solver.service.definition.api.description.ConstraintInfo;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.TimetableConstraintProperties;
import org.acme.schooltimetabling.domain.justification.TimetableJustification.RoomConflictJustification;
import org.acme.schooltimetabling.domain.justification.TimetableJustification.StudentGroupConflictJustification;
import org.acme.schooltimetabling.domain.justification.TimetableJustification.StudentGroupSubjectVarietyJustification;
import org.acme.schooltimetabling.domain.justification.TimetableJustification.TeacherConflictJustification;
import org.acme.schooltimetabling.domain.justification.TimetableJustification.TeacherRoomStabilityJustification;
import org.acme.schooltimetabling.domain.justification.TimetableJustification.TeacherTimeEfficiencyJustification;

public class TimetableConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                roomConflict(constraintFactory),
                teacherConflict(constraintFactory),
                studentGroupConflict(constraintFactory),

                // Soft constraints
                teacherRoomStability(constraintFactory),
                teacherTimeEfficiency(constraintFactory),
                studentGroupSubjectVariety(constraintFactory)
        };
    }

    /**
     * A room can accommodate at most one lesson at the same time.
     */
    public Constraint roomConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                // Select each pair of 2 different lessons ...
                .forEachUniquePair(Lesson.class,
                        // ... in the same timeslot ...
                        Joiners.equal(Lesson::getTimeslot),
                        // ... in the same room ...
                        Joiners.equal(Lesson::getRoom))
                // ... and penalize each pair with a hard weight.
                .penalize(HardSoftScore.ONE_HARD)
                .justifyWith((lesson1, lesson2, score) -> RoomConflictJustification.of(lesson1, lesson2))
                .asConstraint(new ConstraintInfo(TimetableConstraintProperties.ROOM_CONFLICT,
                        TimetableConstraintProperties.ROOM_CONFLICT,
                        "Two lessons cannot be scheduled in the same room at the same time.",
                        TimetableConstraintGroup.CONFLICTS));
    }

    /**
     * A teacher can teach at most one lesson at the same time.
     */
    public Constraint teacherConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getTimeslot),
                        Joiners.equal(Lesson::getTeacher))
                .penalize(HardSoftScore.ONE_HARD)
                .justifyWith((lesson1, lesson2, score) -> TeacherConflictJustification.of(lesson1, lesson2))
                .asConstraint(new ConstraintInfo(TimetableConstraintProperties.TEACHER_CONFLICT,
                        TimetableConstraintProperties.TEACHER_CONFLICT,
                        "A teacher cannot teach two lessons at the same time.",
                        TimetableConstraintGroup.CONFLICTS));
    }

    /**
     * A student can attend at most one lesson at the same time.
     */
    public Constraint studentGroupConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getTimeslot),
                        Joiners.equal(Lesson::getStudentGroup))
                .penalize(HardSoftScore.ONE_HARD)
                .justifyWith((lesson1, lesson2, score) -> StudentGroupConflictJustification.of(lesson1, lesson2))
                .asConstraint(new ConstraintInfo(TimetableConstraintProperties.STUDENT_GROUP_CONFLICT,
                        TimetableConstraintProperties.STUDENT_GROUP_CONFLICT,
                        "A student group cannot attend two lessons at the same time.",
                        TimetableConstraintGroup.CONFLICTS));
    }

    /**
     * A teacher prefers to teach in a single room.
     */
    public Constraint teacherRoomStability(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getTeacher))
                .filter((lesson1, lesson2) -> !lesson1.getRoom().equals(lesson2.getRoom()))
                .penalize(HardSoftScore.ONE_SOFT)
                .justifyWith((lesson1, lesson2, score) -> TeacherRoomStabilityJustification.of(lesson1, lesson2))
                .asConstraint(new ConstraintInfo(TimetableConstraintProperties.TEACHER_ROOM_STABILITY,
                        TimetableConstraintProperties.TEACHER_ROOM_STABILITY,
                        "A teacher should teach all their lessons in the same room.",
                        TimetableConstraintGroup.TEACHER_PREFERENCES));
    }

    /**
     * A teacher prefers to teach sequential lessons and dislikes gaps between lessons.
     */
    public Constraint teacherTimeEfficiency(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Lesson.class)
                .join(Lesson.class,
                        Joiners.equal(Lesson::getTeacher),
                        Joiners.equal(lesson -> lesson.getTimeslot().dayOfWeek()))
                .filter(Lesson::isDirectlyFollowedBy)
                .reward(HardSoftScore.ONE_SOFT)
                .justifyWith((lesson1, lesson2, score) -> TeacherTimeEfficiencyJustification.of(lesson1, lesson2))
                .asConstraint(new ConstraintInfo(TimetableConstraintProperties.TEACHER_TIME_EFFICIENCY,
                        TimetableConstraintProperties.TEACHER_TIME_EFFICIENCY,
                        "A teacher should have consecutive lessons to minimize gaps in their schedule.",
                        TimetableConstraintGroup.TEACHER_PREFERENCES));
    }

    /**
     * A student group dislikes sequential lessons on the same subject.
     */
    public Constraint studentGroupSubjectVariety(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Lesson.class)
                .join(Lesson.class,
                        Joiners.equal(Lesson::getSubject),
                        Joiners.equal(Lesson::getStudentGroup),
                        Joiners.equal(lesson -> lesson.getTimeslot().dayOfWeek()))
                .filter(Lesson::isDirectlyFollowedBy)
                .penalize(HardSoftScore.ONE_SOFT)
                .justifyWith((lesson1, lesson2, score) -> StudentGroupSubjectVarietyJustification.of(lesson1, lesson2))
                .asConstraint(new ConstraintInfo(TimetableConstraintProperties.STUDENT_GROUP_SUBJECT_VARIETY,
                        TimetableConstraintProperties.STUDENT_GROUP_SUBJECT_VARIETY,
                        "A student group should not have the same subject in consecutive timeslots.",
                        TimetableConstraintGroup.STUDENT_GROUP_PREFERENCES));
    }
}
