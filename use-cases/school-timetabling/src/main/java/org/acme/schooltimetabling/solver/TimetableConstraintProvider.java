package org.acme.schooltimetabling.solver;

import java.time.Duration;
import java.util.Objects;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import ai.timefold.solver.service.definition.api.description.ConstraintInfo;

import org.acme.schooltimetabling.domain.Lesson;

public class TimetableConstraintProvider implements ConstraintProvider {

    public static final String ROOM_CONFLICT = "Room conflict";
    public static final String TEACHER_CONFLICT = "Teacher conflict";
    public static final String STUDENT_GROUP_CONFLICT = "Student group conflict";
    public static final String TEACHER_ROOM_STABILITY = "Teacher room stability";
    public static final String TEACHER_TIME_EFFICIENCY = "Teacher time efficiency";
    public static final String STUDENT_GROUP_SUBJECT_VARIETY = "Student group subject variety";

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                roomConflict(constraintFactory),
                teacherConflict(constraintFactory),
                studentGroupConflict(constraintFactory),
                teacherRoomStability(constraintFactory),
                teacherTimeEfficiency(constraintFactory),
                studentGroupSubjectVariety(constraintFactory)
        };
    }

    Constraint roomConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getTimeslot),
                        Joiners.equal(Lesson::getRoom))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint(new ConstraintInfo(ROOM_CONFLICT, ROOM_CONFLICT,
                        "A room can accommodate at most one lesson at the same time.",
                        TimetableConstraintGroup.CONFLICT_AVOIDANCE));
    }

    Constraint teacherConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getTimeslot),
                        Joiners.equal(Lesson::getTeacher))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint(new ConstraintInfo(TEACHER_CONFLICT, TEACHER_CONFLICT,
                        "A teacher can teach at most one lesson at the same time.",
                        TimetableConstraintGroup.CONFLICT_AVOIDANCE));
    }

    Constraint studentGroupConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getTimeslot),
                        Joiners.equal(Lesson::getStudentGroup))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint(new ConstraintInfo(STUDENT_GROUP_CONFLICT, STUDENT_GROUP_CONFLICT,
                        "A student group can attend at most one lesson at the same time.",
                        TimetableConstraintGroup.CONFLICT_AVOIDANCE));
    }

    Constraint teacherRoomStability(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getTeacher))
                .filter((lesson1, lesson2) -> !Objects.equals(lesson1.getRoom(), lesson2.getRoom()))
                .penalize(HardMediumSoftScore.ONE_SOFT)
                .asConstraint(new ConstraintInfo(TEACHER_ROOM_STABILITY, TEACHER_ROOM_STABILITY,
                        "A teacher prefers to teach in a single room.",
                        TimetableConstraintGroup.TEACHER_PREFERENCES));
    }

    Constraint teacherTimeEfficiency(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Lesson.class)
                .join(Lesson.class, Joiners.equal(Lesson::getTeacher),
                        Joiners.equal(lesson -> lesson.getTimeslot().getDayOfWeek()))
                .filter((lesson1, lesson2) -> {
                    Duration between = Duration.between(lesson1.getTimeslot().getEndTime(),
                            lesson2.getTimeslot().getStartTime());
                    return !between.isNegative() && between.compareTo(Duration.ofMinutes(30)) <= 0;
                })
                .reward(HardMediumSoftScore.ONE_SOFT)
                .asConstraint(new ConstraintInfo(TEACHER_TIME_EFFICIENCY, TEACHER_TIME_EFFICIENCY,
                        "A teacher prefers to teach sequential lessons and dislikes gaps between lessons.",
                        TimetableConstraintGroup.TEACHER_PREFERENCES));
    }

    Constraint studentGroupSubjectVariety(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Lesson.class)
                .join(Lesson.class,
                        Joiners.equal(Lesson::getSubject),
                        Joiners.equal(Lesson::getStudentGroup),
                        Joiners.equal(lesson -> lesson.getTimeslot().getDayOfWeek()))
                .filter((lesson1, lesson2) -> {
                    Duration between = Duration.between(lesson1.getTimeslot().getEndTime(),
                            lesson2.getTimeslot().getStartTime());
                    return !between.isNegative() && between.compareTo(Duration.ofMinutes(30)) <= 0;
                })
                .penalize(HardMediumSoftScore.ONE_SOFT)
                .asConstraint(new ConstraintInfo(STUDENT_GROUP_SUBJECT_VARIETY, STUDENT_GROUP_SUBJECT_VARIETY,
                        "A student group dislikes sequential lessons on the same subject.",
                        TimetableConstraintGroup.STUDENT_PREFERENCES));
    }
}
