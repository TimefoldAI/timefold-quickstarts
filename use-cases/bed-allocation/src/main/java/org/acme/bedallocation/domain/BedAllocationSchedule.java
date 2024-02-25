package org.acme.bedallocation.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;


@PlanningSolution
public class BedAllocationSchedule {

    @ProblemFactCollectionProperty
    private List<Specialism> specialismList;
    @ProblemFactCollectionProperty
    private List<Equipment> equipmentList;
    @ProblemFactCollectionProperty
    private List<Department> departmentList;
    @ProblemFactCollectionProperty
    private List<DepartmentSpecialism> departmentSpecialismList;
    @ProblemFactCollectionProperty
    private List<Room> roomList;
    @ProblemFactCollectionProperty
    private List<RoomSpecialism> roomSpecialismList;
    @ProblemFactCollectionProperty
    private List<RoomEquipment> roomEquipmentList;
    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Bed> bedList;
    @ProblemFactCollectionProperty
    private List<Night> nightList;
    @ProblemFactCollectionProperty
    private List<Patient> patientList;
    @ProblemFactCollectionProperty
    private List<AdmissionPart> admissionPartList;
    @ProblemFactCollectionProperty
    private List<RequiredPatientEquipment> requiredPatientEquipmentList;
    @ProblemFactCollectionProperty
    private List<PreferredPatientEquipment> preferredPatientEquipmentList;

    @PlanningEntityCollectionProperty
    private List<BedDesignation> bedDesignationList;

    @PlanningScore
    private HardMediumSoftScore score;

    // No-arg constructor required for Timefold
    public BedAllocationSchedule() {
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public List<Specialism> getSpecialismList() {
        return specialismList;
    }

    public void setSpecialismList(List<Specialism> specialismList) {
        this.specialismList = specialismList;
    }

    public List<Equipment> getEquipmentList() {
        return equipmentList;
    }

    public void setEquipmentList(List<Equipment> equipmentList) {
        this.equipmentList = equipmentList;
    }

    public List<Department> getDepartmentList() {
        return departmentList;
    }

    public void setDepartmentList(List<Department> departmentList) {
        this.departmentList = departmentList;
    }

    public List<DepartmentSpecialism> getDepartmentSpecialismList() {
        return departmentSpecialismList;
    }

    public void setDepartmentSpecialismList(List<DepartmentSpecialism> departmentSpecialismList) {
        this.departmentSpecialismList = departmentSpecialismList;
    }

    public List<Room> getRoomList() {
        return roomList;
    }

    public void setRoomList(List<Room> roomList) {
        this.roomList = roomList;
    }

    public List<RoomSpecialism> getRoomSpecialismList() {
        return roomSpecialismList;
    }

    public void setRoomSpecialismList(List<RoomSpecialism> roomSpecialismList) {
        this.roomSpecialismList = roomSpecialismList;
    }

    public List<RoomEquipment> getRoomEquipmentList() {
        return roomEquipmentList;
    }

    public void setRoomEquipmentList(List<RoomEquipment> roomEquipmentList) {
        this.roomEquipmentList = roomEquipmentList;
    }

    public List<Bed> getBedList() {
        return bedList;
    }

    public void setBedList(List<Bed> bedList) {
        this.bedList = bedList;
    }

    public List<Night> getNightList() {
        return nightList;
    }

    public void setNightList(List<Night> nightList) {
        this.nightList = nightList;
    }

    public List<Patient> getPatientList() {
        return patientList;
    }

    public void setPatientList(List<Patient> patientList) {
        this.patientList = patientList;
    }

    public List<AdmissionPart> getAdmissionPartList() {
        return admissionPartList;
    }

    public void setAdmissionPartList(List<AdmissionPart> admissionPartList) {
        this.admissionPartList = admissionPartList;
    }

    public List<RequiredPatientEquipment> getRequiredPatientEquipmentList() {
        return requiredPatientEquipmentList;
    }

    public void setRequiredPatientEquipmentList(List<RequiredPatientEquipment> requiredPatientEquipmentList) {
        this.requiredPatientEquipmentList = requiredPatientEquipmentList;
    }

    public List<PreferredPatientEquipment> getPreferredPatientEquipmentList() {
        return preferredPatientEquipmentList;
    }

    public void setPreferredPatientEquipmentList(List<PreferredPatientEquipment> preferredPatientEquipmentList) {
        this.preferredPatientEquipmentList = preferredPatientEquipmentList;
    }

    public List<BedDesignation> getBedDesignationList() {
        return bedDesignationList;
    }

    public void setBedDesignationList(List<BedDesignation> bedDesignationList) {
        this.bedDesignationList = bedDesignationList;
    }

    public HardMediumSoftScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftScore score) {
        this.score = score;
    }

}
