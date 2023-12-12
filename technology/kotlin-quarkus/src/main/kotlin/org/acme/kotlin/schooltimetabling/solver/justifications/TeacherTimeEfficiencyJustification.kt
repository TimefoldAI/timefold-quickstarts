package org.acme.kotlin.schooltimetabling.solver.justifications

import ai.timefold.solver.core.api.score.stream.ConstraintJustification
import org.acme.kotlin.schooltimetabling.domain.Lesson

data class TeacherTimeEfficiencyJustification(
    val teacher: String,
    val lesson1: Lesson,
    val lesson2: Lesson,
    val description: String
) :
    ConstraintJustification {

    constructor(
        teacher: String,
        lesson1: Lesson,
        lesson2: Lesson
    ) : this(
        teacher, lesson1, lesson2,
        "Teacher '%s' has 2 consecutive lessons: lesson '%s' for student group '%s' at '%s %s' and lesson '%s' for student group '%s' at '%s %s' (gap)"
            .format(
                teacher,
                lesson1.subject,
                lesson1.studentGroup,
                lesson1.timeslot?.dayOfWeek,
                lesson1.timeslot?.startTime,
                lesson2.subject,
                lesson2.studentGroup,
                lesson2.timeslot?.dayOfWeek,
                lesson2.timeslot?.startTime
            )
    )
}