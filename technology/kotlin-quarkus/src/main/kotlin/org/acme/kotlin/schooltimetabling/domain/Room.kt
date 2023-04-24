package org.acme.kotlin.schooltimetabling.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id


@Entity
class Room {

    @Id
    @GeneratedValue
    var id: Long? = null

    lateinit var name: String

    // No-arg constructor required for Hibernate
    constructor()

    constructor(name: String) {
        this.name = name
    }

    constructor(id: Long?, name: String)
            : this(name) {
        this.id = id
    }

    override fun toString(): String = name

}
