package org.acme.orderpicking.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;

@PlanningSolution
public class OrderPickingSolution {

    /**
     * Defines the available Trolleys.
     * 
     * @see TrolleyStep for more information about the model constructed by the Solver.
     */
    @ValueRangeProvider
    @ProblemFactCollectionProperty
    private List<Trolley> trolleys;

    /**
     * Defines the available TrolleySteps.
     * 
     * @see TrolleyStep for more information about the model constructed by the Solver.
     */
    @ValueRangeProvider
    @PlanningEntityCollectionProperty
    private List<TrolleyStep> trolleySteps;

    @PlanningScore
    private HardSoftLongScore score;

    public OrderPickingSolution() {
        // Marshalling constructor
    }

    public OrderPickingSolution(List<Trolley> trolleys, List<TrolleyStep> trolleySteps) {
        this.trolleys = trolleys;
        this.trolleySteps = trolleySteps;
    }

    public List<Trolley> getTrolleys() {
        return trolleys;
    }

    public void setTrolleys(List<Trolley> trolleys) {
        this.trolleys = trolleys;
    }

    public List<TrolleyStep> getTrolleySteps() {
        return trolleySteps;
    }

    public void setTrolleySteps(List<TrolleyStep> trolleySteps) {
        this.trolleySteps = trolleySteps;
    }

    public HardSoftLongScore getScore() {
        return score;
    }

    public void setScore(HardSoftLongScore score) {
        this.score = score;
    }
}
