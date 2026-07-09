package org.acme.bedallocation.domain;

import java.util.List;

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

import org.acme.bedallocation.dto.BedScheduleInputMetrics;
import org.acme.bedallocation.dto.BedScheduleOutputMetrics;

@PlanningSolution
public class BedSchedule implements SolverModel<HardMediumSoftScore>,
        InputMetricsAware<BedScheduleInputMetrics>, OutputMetricsAware<BedScheduleOutputMetrics> {

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

    public BedSchedule() {
    }

    public BedSchedule(List<Department> departments, List<Room> rooms, List<Bed> beds, List<Stay> stays) {
        this.departments = departments;
        this.rooms = rooms;
        this.beds = beds;
        this.stays = stays;
    }

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
    public BedScheduleInputMetrics getInputMetrics() {
        return new BedScheduleInputMetrics(stays.size(), beds.size(), rooms.size());
    }

    @Override
    public BedScheduleOutputMetrics getOutputMetrics() {
        int assignedStays = (int) stays.stream().filter(stay -> stay.getBed() != null).count();
        int unassignedStays = stays.size() - assignedStays;
        int usedRooms = (int) stays.stream()
                .filter(stay -> stay.getBed() != null)
                .map(Stay::getRoom)
                .distinct()
                .count();
        return new BedScheduleOutputMetrics(assignedStays, unassignedStays, usedRooms);
    }

    @Override
    public String toString() {
        return "BedSchedule{stays: " + stays.size() + ", score: " + score + '}';
    }
}
