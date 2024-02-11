package org.acme.kotlin.schooltimetabling.domain

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty
import ai.timefold.solver.core.api.domain.solution.PlanningScore
import ai.timefold.solver.core.api.domain.solution.PlanningSolution
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore
import ai.timefold.solver.core.api.solver.SolverStatus

@PlanningSolution
data class Timetable (
    val name: String,
    @ProblemFactCollectionProperty
    @ValueRangeProvider
    val timeslots: List<Timeslot>,
    @ProblemFactCollectionProperty
    @ValueRangeProvider
    val rooms: List<Room>,
    @PlanningEntityCollectionProperty
    val lessons: List<Lesson>,
    @PlanningScore
    var score: HardSoftScore? = null,
    // Ignored by Timefold, used by the UI to display solve or stop solving button
    var solverStatus: SolverStatus? = null) {

    // No-arg constructor required for Timefold
    constructor() : this("", emptyList(), emptyList(), emptyList())

    constructor(name: String, score: HardSoftScore?, solverStatus: SolverStatus)
            : this(name, emptyList(), emptyList(), emptyList(), score, solverStatus)

    override fun toString(): String = name

}
