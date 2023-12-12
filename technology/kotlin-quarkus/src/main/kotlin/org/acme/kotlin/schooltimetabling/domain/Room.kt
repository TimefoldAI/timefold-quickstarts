package org.acme.kotlin.schooltimetabling.domain

import ai.timefold.solver.core.api.domain.lookup.PlanningId
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators


@JsonIdentityInfo(
    scope = Room::class,
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property = "id"
)
class Room {

    @PlanningId
    var id: Long? = null

    lateinit var name: String

    // No-arg constructor required for Hibernate
    constructor()

    constructor(id: Long?, name: String) {
        this.id = id
        this.name = name
    }

    override fun toString(): String = name

}
