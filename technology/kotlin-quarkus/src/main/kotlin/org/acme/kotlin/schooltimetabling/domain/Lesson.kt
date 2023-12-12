package org.acme.kotlin.schooltimetabling.domain

import ai.timefold.solver.core.api.domain.entity.PlanningEntity
import ai.timefold.solver.core.api.domain.lookup.PlanningId
import ai.timefold.solver.core.api.domain.variable.PlanningVariable
import com.fasterxml.jackson.annotation.JsonIdentityReference


@PlanningEntity
class Lesson {

    @PlanningId
    var id: Long? = null

    lateinit var subject: String
    lateinit var teacher: String
    lateinit var studentGroup: String

    @JsonIdentityReference
    @PlanningVariable
    var timeslot: Timeslot? = null

    @JsonIdentityReference
    @PlanningVariable
    var room: Room? = null

    // No-arg constructor required for Hibernate and Timefold
    constructor()

    constructor(id: Long?, subject: String, teacher: String, studentGroup: String) {
        this.id = id
        this.subject = subject.trim()
        this.teacher = teacher.trim()
        this.studentGroup = studentGroup.trim()
    }

    constructor(id: Long?, subject: String, teacher: String, studentGroup: String, timeslot: Timeslot?, room: Room?)
            : this(id, subject, teacher, studentGroup) {
        this.timeslot = timeslot
        this.room = room
    }


    override fun toString(): String = "$subject($id)"

}
