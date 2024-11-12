package org.acme.kotlin.schooltimetabling.solver.justifications

import ai.timefold.solver.core.api.score.stream.ConstraintJustification
import org.acme.kotlin.schooltimetabling.domain.Lesson
import org.acme.kotlin.schooltimetabling.domain.Room


data class RoomConflictJustification(
    val room: Room,
    val lesson1: Lesson,
    val lesson2: Lesson,
    val description: String
) :
    ConstraintJustification {

    constructor(
        room: Room,
        lesson1: Lesson,
        lesson2: Lesson
    ) : this(
        room, lesson1, lesson2,
        "Room '%s' is used for lesson '%s' for student group '%s' and lesson '%s' for student group '%s' at '%s %s'"
            .format(
                room,
                lesson1.subject,
                lesson1.studentGroup,
                lesson2.subject,
                lesson2.studentGroup,
                lesson1.timeslot?.dayOfWeek,
                lesson1.timeslot?.startTime
            )
    )
}
