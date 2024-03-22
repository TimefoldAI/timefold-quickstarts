package org.acme.bedallocation.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@PlanningEntity
@JsonIdentityInfo(scope = BedDesignation.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class BedDesignation {

    @PlanningId
    private String id;

    private Stay stay; // TODO merge Stay and BedDesignation and call it Stay
    @PlanningVariable(allowsUnassigned = true)
    private Bed bed;

    // No-arg constructor required for Timefold
    public BedDesignation() {
    }

    public BedDesignation(String id, Stay stay) {
        this.id = id;
        this.stay = stay;
    }

    public BedDesignation(String id, Stay stay, Bed bed) {
        this(id, stay);
        this.bed = bed;
    }


    @JsonIgnore
    public Patient getPatient() {
        return stay.getPatient();
    }

    @JsonIgnore
    public Gender getPatientGender() {
        return stay.getPatient().getGender();
    }

    @JsonIgnore
    public int getPatientAge() {
        return stay.getPatient().getAge();
    }

    @JsonIgnore
    public Integer getPatientPreferredMaximumRoomCapacity() {
        return stay.getPatient().getPreferredMaximumRoomCapacity();
    }

    @JsonIgnore
    public String getSpecialism() {
        return stay.getSpecialism();
    }

    @JsonIgnore
    public int getNightCount() {
        return stay.getNightCount();
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
        return stay.toString();
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public Stay getStay() {
        return stay;
    }

    public void setStay(Stay stay) {
        this.stay = stay;
    }

    public Bed getBed() {
        return bed;
    }

    public void setBed(Bed bed) {
        this.bed = bed;
    }

}
