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
data class Timeslot(
    @PlanningId
    val id: String,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime = startTime.plusMinutes(50)) {

    override fun toString(): String = "$dayOfWeek $startTime"

}
