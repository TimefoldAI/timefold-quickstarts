package org.acme.kotlin.schooltimetabling.domain

import java.time.DayOfWeek
import java.time.LocalTime
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id


@Entity
class Timeslot {

    @Id
    @GeneratedValue
    var id: Long? = null

    lateinit var dayOfWeek: DayOfWeek
    lateinit var startTime: LocalTime
    lateinit var endTime: LocalTime

    // No-arg constructor required for Hibernate
    constructor()

    constructor(dayOfWeek: DayOfWeek, startTime: LocalTime, endTime: LocalTime) {
        this.dayOfWeek = dayOfWeek
        this.startTime = startTime
        this.endTime = endTime
    }

    constructor(id: Long?, dayOfWeek: DayOfWeek, startTime: LocalTime, endTime: LocalTime)
            : this(dayOfWeek, startTime, endTime) {
        this.id = id
    }

    override fun toString(): String = "$dayOfWeek $startTime"

}
