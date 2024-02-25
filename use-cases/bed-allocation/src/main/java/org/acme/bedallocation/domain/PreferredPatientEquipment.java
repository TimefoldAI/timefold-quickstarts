package org.acme.bedallocation.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;


@JsonIdentityInfo(scope = PreferredPatientEquipment.class, generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class PreferredPatientEquipment {

    @PlanningId
    private String id;

    private Patient patient;
    private Equipment equipment;

    public PreferredPatientEquipment() {
    }

    public PreferredPatientEquipment(String id, Patient patient, Equipment equipment) {
        this.id = id;
        this.patient = patient;
        this.equipment = equipment;
    }

    @Override
    public String toString() {
        return patient + "-" + equipment;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

}
