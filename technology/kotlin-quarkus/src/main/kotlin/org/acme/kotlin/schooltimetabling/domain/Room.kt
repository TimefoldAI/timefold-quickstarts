package org.acme.kotlin.schooltimetabling.domain

import ai.timefold.solver.core.api.domain.lookup.PlanningId
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators

@JsonIdentityInfo(
    scope = Room::class,
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property = "id"
)
data class Room(
    @PlanningId
    val id: String,
    val name: String) {

    override fun toString(): String = name

}
