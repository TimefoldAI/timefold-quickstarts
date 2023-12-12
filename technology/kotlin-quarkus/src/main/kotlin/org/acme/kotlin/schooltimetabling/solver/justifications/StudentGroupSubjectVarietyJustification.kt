package org.acme.kotlin.schooltimetabling.solver.justifications

import ai.timefold.solver.core.api.score.stream.ConstraintJustification
import org.acme.kotlin.schooltimetabling.domain.Lesson

data class StudentGroupSubjectVarietyJustification(
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
        "Student Group '%s' has two consecutive lessons on '%s' at '%s %s' and at '%s %s'"
            .format(
                studentGroup,
                lesson1.subject,
                lesson1.timeslot?.dayOfWeek,
                lesson1.timeslot?.startTime,
                lesson2.timeslot?.dayOfWeek,
                lesson2.timeslot?.startTime
            )
    )
}