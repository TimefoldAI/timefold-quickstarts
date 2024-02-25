package org.acme.bedallocation.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

@PlanningEntity
@JsonIdentityInfo(scope = BedDesignation.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class BedDesignation {

    @PlanningId
    private String id;

    private AdmissionPart admissionPart;
    @PlanningVariable(allowsUnassigned = true)
    private Bed bed;

    // No-arg constructor required for Timefold
    public BedDesignation() {
    }

    public BedDesignation(String id, AdmissionPart admissionPart) {
        this.id = id;
        this.admissionPart = admissionPart;
    }

    public BedDesignation(String id, AdmissionPart admissionPart, Bed bed) {
        this(id, admissionPart);
        this.bed = bed;
    }


    @JsonIgnore
    public Patient getPatient() {
        return admissionPart.getPatient();
    }

    @JsonIgnore
    public Gender getPatientGender() {
        return admissionPart.getPatient().getGender();
    }

    @JsonIgnore
    public int getPatientAge() {
        return admissionPart.getPatient().getAge();
    }

    @JsonIgnore
    public Integer getPatientPreferredMaximumRoomCapacity() {
        return admissionPart.getPatient().getPreferredMaximumRoomCapacity();
    }

    @JsonIgnore
    public Specialism getAdmissionPartSpecialism() {
        return admissionPart.getSpecialism();
    }

    @JsonIgnore
    public int getFirstNightIndex() {
        return admissionPart.getFirstNight().getIndex();
    }

    @JsonIgnore
    public int getLastNightIndex() {
        return admissionPart.getLastNight().getIndex();
    }

    @JsonIgnore
    public int getAdmissionPartNightCount() {
        return admissionPart.getNightCount();
    }

    @JsonIgnore
    public Room getRoom() {
        if (bed == null) {
            return null;
        }
        return bed.getRoom();
    }

    @JsonIgnore
    public int getRoomCapacity() {
        if (bed == null) {
            return Integer.MIN_VALUE;
        }
        return bed.getRoom().getCapacity();
    }

    @JsonIgnore
    public Department getDepartment() {
        if (bed == null) {
            return null;
        }
        return bed.getRoom().getDepartment();
    }

    @JsonIgnore
    public GenderLimitation getRoomGenderLimitation() {
        if (bed == null) {
            return null;
        }
        return bed.getRoom().getGenderLimitation();
    }

    @Override
    public String toString() {
        return admissionPart.toString();
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public AdmissionPart getAdmissionPart() {
        return admissionPart;
    }

    public void setAdmissionPart(AdmissionPart admissionPart) {
        this.admissionPart = admissionPart;
    }

    public Bed getBed() {
        return bed;
    }

    public void setBed(Bed bed) {
        this.bed = bed;
    }

}
