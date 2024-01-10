package org.acme.kotlin.schooltimetabling.solver.justifications

import ai.timefold.solver.core.api.score.stream.ConstraintJustification
import org.acme.kotlin.schooltimetabling.domain.Lesson

data class TeacherRoomStabilityJustification(
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
        "Teacher '%s' has two lessons in different rooms: room '%s' at '%s %s' and room '%s' at '%s %s'"
            .format(
                teacher,
                lesson1.room,
                lesson1.studentGroup,
                lesson1.timeslot?.dayOfWeek,
                lesson1.timeslot?.startTime,
                lesson2.room,
                lesson2.timeslot?.dayOfWeek,
                lesson2.timeslot?.startTime
            )
    )
}