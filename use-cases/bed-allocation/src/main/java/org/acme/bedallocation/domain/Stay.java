package org.acme.bedallocation.domain;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

import com.fasterxml.jackson.annotation.JsonIgnore;

@PlanningEntity
public class Stay {

    @PlanningId
    private String id;

    private Patient patient;
    private LocalDate arrivalDate;
    private LocalDate departureDate;
    private String specialism;
    @PlanningVariable(allowsUnassigned = true)
    private Bed bed;

    public Stay() {
    }

    public Stay(String id, Patient patient) {
        this.id = id;
        this.patient = patient;
    }

    public Stay(String id, Patient patient, LocalDate arrivalDate, LocalDate departureDate, String specialism, Bed bed) {
        this.id = id;
        this.patient = patient;
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
        this.specialism = specialism;
        this.bed = bed;
    }

    @JsonIgnore
    public int getNightCount() {
        return (int) DAYS.between(arrivalDate, departureDate) + 1; // TODO is + 1 still desired?
    }

    public int calculateSameNightCount(Stay other) {
        LocalDate maxArrivalDate = arrivalDate.compareTo(other.arrivalDate) < 0 ? other.arrivalDate : arrivalDate;
        LocalDate minDepartureDate = departureDate.compareTo(other.departureDate) < 0 ? departureDate : other.departureDate;
        return Math.max(0, (int) DAYS.between(maxArrivalDate, minDepartureDate) + 1); // TODO is + 1 still desired?
    }

    @JsonIgnore
    public Gender getPatientGender() {
        return getPatient().getGender();
    }

    @JsonIgnore
    public int getPatientAge() {
        return getPatient().getAge();
    }

    @JsonIgnore
    public Integer getPatientPreferredMaximumRoomCapacity() {
        return getPatient().getPreferredMaximumRoomCapacity();
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
        return patient + "(" + arrivalDate + "-" + departureDate + ")";
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

    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public String getSpecialism() {
        return specialism;
    }

    public void setSpecialism(String specialism) {
        this.specialism = specialism;
    }

    public Bed getBed() {
        return bed;
    }

    public void setBed(Bed bed) {
        this.bed = bed;
    }
}
