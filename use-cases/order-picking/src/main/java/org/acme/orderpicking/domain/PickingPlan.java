package org.acme.orderpicking.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.service.definition.api.SolverModel;
import ai.timefold.solver.service.definition.api.metrics.InputMetricsAware;
import ai.timefold.solver.service.definition.api.metrics.OutputMetricsAware;

import org.acme.orderpicking.dto.input.PickingPlanInputMetrics;
import org.acme.orderpicking.dto.output.PickingPlanOutputMetrics;

@PlanningSolution
public class PickingPlan implements SolverModel<HardSoftScore>,
        InputMetricsAware<PickingPlanInputMetrics>, OutputMetricsAware<PickingPlanOutputMetrics> {

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
    private HardSoftScore score;

    private ConstraintWeightOverrides<HardSoftScore> constraintWeightOverrides = ConstraintWeightOverrides.none();

    public PickingPlan() {
    }

    public PickingPlan(List<Trolley> trolleys, List<PickTask> pickTasks) {
        this.trolleys = trolleys;
        this.pickTasks = pickTasks;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public List<Trolley> getTrolleys() {
        return trolleys;
    }

    public List<PickTask> getPickTasks() {
        return pickTasks;
    }

    @Override
    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    @Override
    public ConstraintWeightOverrides<HardSoftScore> getConstraintWeightOverrides() {
        return constraintWeightOverrides;
    }

    public void setConstraintWeightOverrides(ConstraintWeightOverrides<HardSoftScore> constraintWeightOverrides) {
        this.constraintWeightOverrides = constraintWeightOverrides;
    }

    @Override
    public PickingPlanInputMetrics getInputMetrics() {
        int orders = (int) pickTasks.stream().map(PickTask::getOrderId).distinct().count();
        int products = (int) pickTasks.stream().map(PickTask::getProduct).distinct().count();
        return new PickingPlanInputMetrics(pickTasks.size(), orders, products, trolleys.size());
    }

    // Reads the trolleys' list variable rather than the pick tasks' inverse relation shadow variable, so the metrics
    // are correct even before the solver has computed the shadow variables.
    @Override
    public PickingPlanOutputMetrics getOutputMetrics() {
        int totalTravelDistance = 0;
        int usedTrolleys = 0;
        Map<String, Set<Trolley>> trolleysByOrderId = new HashMap<>();
        for (Trolley trolley : trolleys) {
            totalTravelDistance += Warehouse.calculateDistanceToTravel(trolley);
            if (!trolley.getPickTasks().isEmpty()) {
                usedTrolleys++;
            }
            for (PickTask pickTask : trolley.getPickTasks()) {
                trolleysByOrderId.computeIfAbsent(pickTask.getOrderId(), orderId -> new HashSet<>()).add(trolley);
            }
        }
        int splitOrders = (int) trolleysByOrderId.values().stream().filter(orderTrolleys -> orderTrolleys.size() > 1)
                .count();
        return new PickingPlanOutputMetrics(totalTravelDistance, usedTrolleys, splitOrders);
    }
}
