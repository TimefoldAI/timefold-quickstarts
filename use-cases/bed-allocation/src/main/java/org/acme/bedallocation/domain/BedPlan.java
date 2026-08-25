package org.acme.bedallocation.domain;

import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.service.definition.api.SolverModel;
import ai.timefold.solver.service.definition.api.metrics.InputMetricsAware;
import ai.timefold.solver.service.definition.api.metrics.OutputMetricsAware;

import org.acme.bedallocation.dto.input.BedPlanInputMetrics;
import org.acme.bedallocation.dto.output.BedPlanOutputMetrics;

@PlanningSolution
public class BedPlan implements SolverModel<HardMediumSoftScore>,
        InputMetricsAware<BedPlanInputMetrics>, OutputMetricsAware<BedPlanOutputMetrics> {

    @ProblemFactCollectionProperty
    private List<Department> departments;
    @ProblemFactCollectionProperty
    private List<Room> rooms;
    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Bed> beds;
    @PlanningEntityCollectionProperty
    private List<Stay> stays;

    @PlanningScore
    private HardMediumSoftScore score;

    private ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides = ConstraintWeightOverrides.none();

    public BedPlan() {
    }

    public BedPlan(List<Department> departments, List<Room> rooms, List<Bed> beds, List<Stay> stays) {
        this.departments = departments;
        this.rooms = rooms;
        this.beds = beds;
        this.stays = stays;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    public List<Bed> getBeds() {
        return beds;
    }

    public void setBeds(List<Bed> beds) {
        this.beds = beds;
    }

    public List<Stay> getStays() {
        return stays;
    }

    public void setStays(List<Stay> stays) {
        this.stays = stays;
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
    public BedPlanInputMetrics getInputMetrics() {
        return new BedPlanInputMetrics(stays.size(), departments.size(), rooms.size(), beds.size());
    }

    @Override
    public BedPlanOutputMetrics getOutputMetrics() {
        int assignedStays = (int) stays.stream().filter(stay -> stay.getBed() != null).count();
        int unassignedStays = stays.size() - assignedStays;
        int usedBeds = (int) stays.stream().map(Stay::getBed).filter(Objects::nonNull).distinct().count();
        int usedRooms = (int) stays.stream().map(Stay::getRoom).filter(Objects::nonNull).distinct().count();
        return new BedPlanOutputMetrics(assignedStays, unassignedStays, usedRooms, usedBeds);
    }
}
