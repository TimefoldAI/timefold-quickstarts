package org.acme.orderpicking.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;

@PlanningSolution
public class OrderPickingSolution {

    /**
     * Defines the available Trolleys.
     * 
     * @see PickTask for more information about the model constructed by the Solver.
     */
    @PlanningEntityCollectionProperty
    private List<Trolley> trolleys;

    /**
     * Defines the available PickTasks.
     *
     * @see PickTask for more information about the model constructed by the Solver.
     */
    @ValueRangeProvider
    @PlanningEntityCollectionProperty
    private List<PickTask> pickTasks;

    @PlanningScore
    private HardSoftLongScore score;

    public OrderPickingSolution() {
        // Marshalling constructor
    }

    public OrderPickingSolution(List<Trolley> trolleys, List<PickTask> pickTasks) {
        this.trolleys = trolleys;
        this.pickTasks = pickTasks;
    }

    public List<Trolley> getTrolleys() {
        return trolleys;
    }

    public void setTrolleys(List<Trolley> trolleys) {
        this.trolleys = trolleys;
    }

    public List<PickTask> getPickTasks() {
        return pickTasks;
    }

    public void setPickTasks(List<PickTask> picks) {
        this.pickTasks = picks;
    }

    public HardSoftLongScore getScore() {
        return score;
    }

    public void setScore(HardSoftLongScore score) {
        this.score = score;
    }
}
