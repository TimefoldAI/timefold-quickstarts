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
     * Defines the available picking carts.
     *
     * @see PickTask for more information about the model constructed by the Solver.
     */
    @PlanningEntityCollectionProperty
    private List<PickingCart> pickingCarts;

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

    public PickingPlan(List<PickingCart> pickingCarts, List<PickTask> pickTasks) {
        this.pickingCarts = pickingCarts;
        this.pickTasks = pickTasks;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public List<PickingCart> getPickingCarts() {
        return pickingCarts;
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
        return new PickingPlanInputMetrics(pickTasks.size(), orders, products, pickingCarts.size());
    }

    // Reads the picking carts' list variable rather than the pick tasks' inverse relation shadow variable, so the
    // metrics are correct even before the solver has computed the shadow variables.
    @Override
    public PickingPlanOutputMetrics getOutputMetrics() {
        int totalTravelDistance = 0;
        int usedPickingCarts = 0;
        Map<String, Set<PickingCart>> pickingCartsByOrderId = new HashMap<>();
        for (PickingCart pickingCart : pickingCarts) {
            totalTravelDistance += Warehouse.calculateDistanceToTravel(pickingCart);
            if (!pickingCart.getPickTasks().isEmpty()) {
                usedPickingCarts++;
            }
            for (PickTask pickTask : pickingCart.getPickTasks()) {
                pickingCartsByOrderId.computeIfAbsent(pickTask.getOrderId(), orderId -> new HashSet<>()).add(pickingCart);
            }
        }
        int splitOrders =
                (int) pickingCartsByOrderId.values().stream().filter(orderPickingCarts -> orderPickingCarts.size() > 1)
                        .count();
        return new PickingPlanOutputMetrics(totalTravelDistance, usedPickingCarts, splitOrders);
    }
}
