package org.acme.orderpicking.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.service.definition.api.SolverModel;
import ai.timefold.solver.service.definition.api.metrics.InputMetricsAware;
import ai.timefold.solver.service.definition.api.metrics.OutputMetricsAware;

import org.acme.orderpicking.dto.OrderPickingInputMetrics;
import org.acme.orderpicking.dto.OrderPickingOutputMetrics;

@PlanningSolution
public class OrderPickingSolution implements SolverModel<HardMediumSoftScore>,
        InputMetricsAware<OrderPickingInputMetrics>, OutputMetricsAware<OrderPickingOutputMetrics> {

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
    private HardMediumSoftScore score;

    private ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides = ConstraintWeightOverrides.none();

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

    @Override
    public HardMediumSoftScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftScore score) {
        this.score = score;
    }

    @Override
    public ConstraintWeightOverrides<HardMediumSoftScore> getConstraintWeightOverrides() {
        return constraintWeightOverrides;
    }

    public void setConstraintWeightOverrides(ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides) {
        this.constraintWeightOverrides = constraintWeightOverrides;
    }

    @Override
    public OrderPickingInputMetrics getInputMetrics() {
        long orders = pickTasks.stream().map(pick -> pick.getOrderItem().getOrder()).distinct().count();
        long products = pickTasks.stream().map(pick -> pick.getOrderItem().getProduct()).distinct().count();
        long totalVolume = pickTasks.stream().mapToLong(pick -> pick.getOrderItem().getVolume()).sum();
        return new OrderPickingInputMetrics(trolleys.size(), (int) orders, pickTasks.size(), (int) products, totalVolume);
    }

    @Override
    public OrderPickingOutputMetrics getOutputMetrics() {
        int assignedPickTasks = (int) pickTasks.stream().filter(pick -> pick.getTrolley() != null).count();
        int unassignedPickTasks = pickTasks.size() - assignedPickTasks;
        int usedTrolleys = (int) trolleys.stream().filter(trolley -> !trolley.getPickTasks().isEmpty()).count();
        long totalDistance = trolleys.stream()
                .filter(trolley -> !trolley.getPickTasks().isEmpty())
                .mapToLong(Warehouse::calculateDistanceToTravel)
                .sum();
        return new OrderPickingOutputMetrics(assignedPickTasks, unassignedPickTasks, usedTrolleys, totalDistance);
    }

    @Override
    public String toString() {
        return "OrderPickingSolution{pickTasks: " + pickTasks.size() + ", score: " + score + '}';
    }
}
