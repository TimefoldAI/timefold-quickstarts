package org.acme.kotlin.schooltimetabling.solver

import org.acme.kotlin.schooltimetabling.domain.Lesson
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore
import ai.timefold.solver.core.api.score.stream.Constraint
import ai.timefold.solver.core.api.score.stream.ConstraintFactory
import ai.timefold.solver.core.api.score.stream.ConstraintProvider
import ai.timefold.solver.core.api.score.stream.Joiners
import java.time.Duration

class TimeTableConstraintProvider : ConstraintProvider {

    override fun defineConstraints(constraintFactory: ConstraintFactory): Array<Constraint>? {
        return arrayOf(
            // Hard constraints
            roomConflict(constraintFactory),
            teacherConflict(constraintFactory),
            studentGroupConflict(constraintFactory),
            // Soft constraints
            teacherRoomStability(constraintFactory),
            teacherTimeEfficiency(constraintFactory),
            studentGroupSubjectVariety(constraintFactory)
        )
    }

    fun roomConflict(constraintFactory: ConstraintFactory): Constraint {
        // A room can accommodate at most one lesson at the same time.
        return constraintFactory
            // Select each pair of 2 different lessons ...
            .forEachUniquePair(
                Lesson::class.java,
                // ... in the same timeslot ...
                Joiners.equal(Lesson::timeslot),
                // ... in the same room ...
                Joiners.equal(Lesson::room)
            )
            // ... and penalize each pair with a hard weight.
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Room conflict");
    }

    fun teacherConflict(constraintFactory: ConstraintFactory): Constraint {
        // A teacher can teach at most one lesson at the same time.
        return constraintFactory
            .forEachUniquePair(
                Lesson::class.java,
                Joiners.equal(Lesson::timeslot),
                Joiners.equal(Lesson::teacher)
            )
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Teacher conflict");
    }

    fun studentGroupConflict(constraintFactory: ConstraintFactory): Constraint {
        // A student can attend at most one lesson at the same time.
        return constraintFactory
            .forEachUniquePair(
                Lesson::class.java,
                Joiners.equal(Lesson::timeslot),
                Joiners.equal(Lesson::studentGroup)
            )
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Student group conflict");
    }

    fun teacherRoomStability(constraintFactory: ConstraintFactory): Constraint {
        // A teacher prefers to teach in a single room.
        return constraintFactory
            .forEachUniquePair(
                Lesson::class.java,
                Joiners.equal(Lesson::teacher)
            )
            .filter { lesson1: Lesson, lesson2: Lesson -> lesson1.room !== lesson2.room }
            .penalize(HardSoftScore.ONE_SOFT)
            .asConstraint("Teacher room stability");
    }

    fun teacherTimeEfficiency(constraintFactory: ConstraintFactory): Constraint {
        // A teacher prefers to teach sequential lessons and dislikes gaps between lessons.
        return constraintFactory
            .forEach(Lesson::class.java)
            .join(Lesson::class.java,
                Joiners.equal(Lesson::teacher),
                Joiners.equal { lesson: Lesson -> lesson.timeslot?.dayOfWeek })
            .filter { lesson1: Lesson, lesson2: Lesson ->
                val between = Duration.between(
                    lesson1.timeslot?.endTime,
                    lesson2.timeslot?.startTime
                )
                !between.isNegative && between.compareTo(Duration.ofMinutes(30)) <= 0
            }
            .reward(HardSoftScore.ONE_SOFT)
            .asConstraint("Teacher time efficiency");
    }

    fun studentGroupSubjectVariety(constraintFactory: ConstraintFactory): Constraint {
        // A student group dislikes sequential lessons on the same subject.
        return constraintFactory
            .forEach(Lesson::class.java)
            .join(Lesson::class.java,
                Joiners.equal(Lesson::subject),
                Joiners.equal(Lesson::studentGroup),
                Joiners.equal { lesson: Lesson -> lesson.timeslot?.dayOfWeek })
            .filter { lesson1: Lesson, lesson2: Lesson ->
                val between = Duration.between(
                    lesson1.timeslot?.endTime,
                    lesson2.timeslot?.startTime
                )
                !between.isNegative && between.compareTo(Duration.ofMinutes(30)) <= 0
            }
            .penalize(HardSoftScore.ONE_SOFT)
            .asConstraint("Student group subject variety");
    }

}
