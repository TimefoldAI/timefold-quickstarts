package org.acme.kotlin.schooltimetabling.domain

import ai.timefold.solver.core.api.domain.lookup.PlanningId
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import java.time.DayOfWeek
import java.time.LocalTime


@JsonIdentityInfo(
    scope = Timeslot::class,
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property = "id"
)
class Timeslot {

    @PlanningId
    var id: Long? = null

    lateinit var dayOfWeek: DayOfWeek
    lateinit var startTime: LocalTime
    lateinit var endTime: LocalTime

    // No-arg constructor required for Hibernate
    constructor()

    constructor(id: Long?, dayOfWeek: DayOfWeek, startTime: LocalTime, endTime: LocalTime) {
        this.id = id
        this.dayOfWeek = dayOfWeek
        this.startTime = startTime
        this.endTime = endTime
    }

    constructor(id: Long?, dayOfWeek: DayOfWeek, startTime: LocalTime) :
            this(id, dayOfWeek, startTime, startTime.plusMinutes(50))

    override fun toString(): String = "$dayOfWeek $startTime"

}
