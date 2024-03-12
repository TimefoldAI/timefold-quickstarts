package org.acme.kotlin.schooltimetabling.domain

import ai.timefold.solver.core.api.domain.entity.PlanningEntity
import ai.timefold.solver.core.api.domain.lookup.PlanningId
import ai.timefold.solver.core.api.domain.variable.PlanningVariable
import com.fasterxml.jackson.annotation.JsonIdentityReference


@PlanningEntity
data class Lesson (
    @PlanningId
    val id: String,
    val subject: String,
    val teacher: String,
    val studentGroup: String) {

    @JsonIdentityReference
    @PlanningVariable
    var timeslot: Timeslot? = null

    @JsonIdentityReference
    @PlanningVariable
    var room: Room? = null

    // No-arg constructor required for Timefold
    constructor() : this("0", "", "", "")

    constructor(id: String, subject: String, teacher: String, studentGroup: String, timeslot: Timeslot?, room: Room?)
            : this(id, subject, teacher, studentGroup) {
        this.timeslot = timeslot
        this.room = room
    }

    override fun toString(): String = "$subject($id)"

}
