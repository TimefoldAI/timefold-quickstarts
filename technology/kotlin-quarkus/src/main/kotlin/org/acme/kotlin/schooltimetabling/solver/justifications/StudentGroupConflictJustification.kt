package org.acme.kotlin.schooltimetabling.solver.justifications

import ai.timefold.solver.core.api.score.stream.ConstraintJustification
import org.acme.kotlin.schooltimetabling.domain.Lesson


data class StudentGroupConflictJustification(
    val studentGroup: String,
    val lesson1: Lesson,
    val lesson2: Lesson,
    val description: String
) :
    ConstraintJustification {

    constructor(
        studentGroup: String,
        lesson1: Lesson,
        lesson2: Lesson
    ) : this(
        studentGroup, lesson1, lesson2,
        "Student group '%s' has lesson '%s' and lesson '%s' at '%s %s'"
            .format(
                studentGroup,
                lesson1.subject,
                lesson2.subject,
                lesson2.studentGroup,
                lesson1.timeslot?.dayOfWeek,
                lesson1.timeslot?.startTime
            )
    )
}
