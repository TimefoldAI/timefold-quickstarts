package org.acme.bedallocation.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.solver.SolverStatus;

@PlanningSolution
public class Schedule {

    @ProblemFactCollectionProperty
    private List<Specialism> specialisms;
    @ProblemFactCollectionProperty
    private List<Department> departments;
    @ProblemFactCollectionProperty
    private List<DepartmentSpecialism> departmentSpecialisms;
    @ProblemFactCollectionProperty
    private List<Room> rooms;
    @ProblemFactCollectionProperty
    private List<RoomSpecialism> roomSpecialisms;
    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Bed> beds;
    @ProblemFactCollectionProperty
    private List<Patient> patients;
    @ProblemFactCollectionProperty
    private List<Stay> stays;

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

    public List<Specialism> getSpecialisms() {
        return specialisms;
    }

    public void setSpecialisms(List<Specialism> specialisms) {
        this.specialisms = specialisms;
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public List<DepartmentSpecialism> getDepartmentSpecialisms() {
        return departmentSpecialisms;
    }

    public void setDepartmentSpecialisms(List<DepartmentSpecialism> departmentSpecialisms) {
        this.departmentSpecialisms = departmentSpecialisms;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    public List<RoomSpecialism> getRoomSpecialisms() {
        return roomSpecialisms;
    }

    public void setRoomSpecialisms(List<RoomSpecialism> roomSpecialisms) {
        this.roomSpecialisms = roomSpecialisms;
    }

    public List<Bed> getBeds() {
        return beds;
    }

    public void setBeds(List<Bed> beds) {
        this.beds = beds;
    }

    public List<Patient> getPatients() {
        return patients;
    }

    public void setPatients(List<Patient> patients) {
        this.patients = patients;
    }

    public List<Stay> getStays() {
        return stays;
    }

    public void setStays(List<Stay> stays) {
        this.stays = stays;
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
