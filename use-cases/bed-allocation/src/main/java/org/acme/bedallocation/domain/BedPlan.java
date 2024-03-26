package org.acme.bedallocation.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.solver.SolverStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;

@PlanningSolution
public class BedPlan {

    private List<Department> departments;

    @PlanningEntityCollectionProperty
    private List<Stay> stays;

    @PlanningScore
    private HardMediumSoftScore score;

    private SolverStatus solverStatus;

    // No-arg constructor required for Timefold
    public BedPlan() {
    }

    public BedPlan(HardMediumSoftScore score, SolverStatus solverStatus) {
        this.score = score;
        this.solverStatus = solverStatus;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************
    @ProblemFactCollectionProperty
    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    @JsonIgnore
    @ProblemFactCollectionProperty
    public List<DepartmentSpecialty> getDepartmentSpecialties() {
        return departments.stream()
                .flatMap(d -> d.getSpecialtyToPriority().entrySet().stream()
                        .map(e -> new DepartmentSpecialty("%s-%s".formatted(d.getId(), e.getKey()), d, e.getKey(),
                                e.getValue()))
                        .toList()
                        .stream())
                .toList();
    }

    @JsonIgnore
    @ProblemFactCollectionProperty
    public List<Room> getRooms() {
        return departments.stream().flatMap(d -> d.getRooms().stream()).toList();
    }

    @JsonIgnore
    @ProblemFactCollectionProperty
    @ValueRangeProvider
    public List<Bed> getBeds() {
        return departments.stream()
                .flatMap(d -> d.getRooms().stream())
                .flatMap(r -> r.getBeds().stream())
                .toList();
    }

    public List<Stay> getStays() {
        return stays;
    }

    public void setStays(List<Stay> stays) {
        this.stays = stays;
    }

    public HardMediumSoftScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftScore score) {
        this.score = score;
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }

}
