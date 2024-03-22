package org.acme.bedallocation.domain;

import static java.util.Collections.emptyList;

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
public class Schedule {

    private List<String> specialisms = emptyList();
    private List<Department> departments;
    private List<DepartmentSpecialism> departmentSpecialisms = emptyList();
    private List<Room> rooms = emptyList();
    private List<RoomSpecialism> roomSpecialisms = emptyList();
    private List<Bed> beds = emptyList();
    private List<Patient> patients = emptyList();
    private List<Stay> stays = emptyList();

    @PlanningEntityCollectionProperty
    private List<BedDesignation> bedDesignations;

    @PlanningScore
    private HardMediumSoftScore score;

    private SolverStatus solverStatus;

    // No-arg constructor required for Timefold
    public Schedule() {
    }

    public Schedule(HardMediumSoftScore score, SolverStatus solverStatus) {
        this.score = score;
        this.solverStatus = solverStatus;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public void setSpecialisms(List<String> specialisms) {
        this.specialisms = specialisms;
    }

    @JsonIgnore
    @ProblemFactCollectionProperty
    public List<String> getSpecialisms() {
        return this.specialisms;
    }

    @ProblemFactCollectionProperty
    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public void setDepartmentSpecialisms(List<DepartmentSpecialism> departmentSpecialisms) {
        this.departmentSpecialisms = departmentSpecialisms;
    }

    @JsonIgnore
    @ProblemFactCollectionProperty
    public List<DepartmentSpecialism> getDepartmentSpecialisms() {
        return this.departmentSpecialisms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    @JsonIgnore
    @ProblemFactCollectionProperty
    public List<Room> getRooms() {
        return this.rooms;
    }

    public void setRoomSpecialisms(List<RoomSpecialism> roomSpecialisms) {
        this.roomSpecialisms = roomSpecialisms;
    }

    @JsonIgnore
    @ProblemFactCollectionProperty
    public List<RoomSpecialism> getRoomSpecialisms() {
        return roomSpecialisms;
    }

    public void setBeds(List<Bed> beds) {
        this.beds = beds;
    }

    @JsonIgnore
    @ProblemFactCollectionProperty
    @ValueRangeProvider
    public List<Bed> getBeds() {
        return this.beds;
    }

    public void setPatients(List<Patient> patients) {
        this.patients = patients;
    }

    @JsonIgnore
    @ProblemFactCollectionProperty
    public List<Patient> getPatients() {
        return this.patients;
    }

    public void setStays(List<Stay> stays) {
        this.stays = stays;
    }

    @JsonIgnore
    @ProblemFactCollectionProperty
    public List<Stay> getStays() {
        return this.stays;
    }

    public List<BedDesignation> getBedDesignations() {
        return bedDesignations;
    }

    public void setBedDesignations(List<BedDesignation> bedDesignations) {
        this.bedDesignations = bedDesignations;
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
