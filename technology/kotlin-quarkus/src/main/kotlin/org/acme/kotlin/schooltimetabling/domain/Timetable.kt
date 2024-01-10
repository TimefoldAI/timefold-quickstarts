package org.acme.kotlin.schooltimetabling.domain

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty
import ai.timefold.solver.core.api.domain.solution.PlanningScore
import ai.timefold.solver.core.api.domain.solution.PlanningSolution
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore
import ai.timefold.solver.core.api.solver.SolverStatus

@PlanningSolution
class Timetable {

    lateinit var name: String

    @ProblemFactCollectionProperty
    @ValueRangeProvider
    lateinit var timeslots: List<Timeslot>

    @ProblemFactCollectionProperty
    @ValueRangeProvider
    lateinit var rooms: List<Room>

    @PlanningEntityCollectionProperty
    lateinit var lessons: List<Lesson>

    @PlanningScore
    var score: HardSoftScore? = null

    // Ignored by Timefold, used by the UI to display solve or stop solving button
    var solverStatus: SolverStatus? = null

    // No-arg constructor required for Timefold
    constructor() {}

    constructor(name: String, score: HardSoftScore?, solverStatus: SolverStatus) {
        this.name = name
        this.score = score
        this.solverStatus = solverStatus
        this.timeslots = emptyList()
        this.rooms = emptyList()
        this.lessons = emptyList()
    }

    constructor(name: String, timeslots: List<Timeslot>, rooms: List<Room>, lessons: List<Lesson>) {
        this.name = name
        this.timeslots = timeslots
        this.rooms = rooms
        this.lessons = lessons
    }
}
