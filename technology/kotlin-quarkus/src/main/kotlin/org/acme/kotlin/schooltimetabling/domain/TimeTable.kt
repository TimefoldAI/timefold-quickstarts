package org.acme.kotlin.schooltimetabling.domain

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty
import ai.timefold.solver.core.api.domain.solution.PlanningScore
import ai.timefold.solver.core.api.domain.solution.PlanningSolution
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore
import ai.timefold.solver.core.api.solver.SolverStatus

@PlanningSolution
class TimeTable {

    @ProblemFactCollectionProperty
    @ValueRangeProvider
    lateinit var timeslotList: List<Timeslot>
    @ProblemFactCollectionProperty
    @ValueRangeProvider
    lateinit var roomList: List<Room>
    @PlanningEntityCollectionProperty
    lateinit var lessonList: List<Lesson>

    @PlanningScore
    var score: HardSoftScore? = null

    // Ignored by OptaPlanner, used by the UI to display solve or stop solving button
    var solverStatus: SolverStatus? = null

    // No-arg constructor required for OptaPlanner
    constructor() {}

    constructor(timeslotList: List<Timeslot>, roomList: List<Room>, lessonList: List<Lesson>) {
        this.timeslotList = timeslotList
        this.roomList = roomList
        this.lessonList = lessonList
    }

}
