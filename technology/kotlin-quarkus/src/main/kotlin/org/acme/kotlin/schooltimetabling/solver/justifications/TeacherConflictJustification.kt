package org.acme.kotlin.schooltimetabling.solver.justifications

import ai.timefold.solver.core.api.score.stream.ConstraintJustification
import org.acme.kotlin.schooltimetabling.domain.Lesson

data class TeacherConflictJustification(
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
        "Teacher '%s' needs to teach lesson '%s' for student group '%s' and lesson '%s' for student group '%s' at '%s %s'"
            .format(
                teacher,
                lesson1.subject,
                lesson1.studentGroup,
                lesson2.subject,
                lesson2.studentGroup,
                lesson1.timeslot?.dayOfWeek,
                lesson1.timeslot?.startTime
            )
    )
}