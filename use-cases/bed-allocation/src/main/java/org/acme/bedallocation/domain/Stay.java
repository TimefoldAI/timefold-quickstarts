package org.acme.bedallocation.domain;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

import com.fasterxml.jackson.annotation.JsonIgnore;

@PlanningEntity
public class Stay {

    @PlanningId
    private String id;
    private String patientName;
    private Gender patientGender;
    private int patientAge;
    private Integer patientPreferredMaximumRoomCapacity;
    private List<String> patientRequiredEquipments;
    private List<String> patientPreferredEquipments;
    private LocalDate arrivalDate;
    private LocalDate departureDate;
    private String specialty;
    @PlanningVariable(allowsUnassigned = true)
    private Bed bed;

    public Stay() {
    }

    public Stay(String id, String patientName) {
        this.id = id;
        this.patientName = patientName;
        this.patientRequiredEquipments = new LinkedList<>();
        this.patientPreferredEquipments = new LinkedList<>();
    }

    public Stay(String id, LocalDate arrivalDate, LocalDate departureDate, String specialty, Bed bed) {
        this.id = id;
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
        this.specialty = specialty;
        this.bed = bed;
        this.patientRequiredEquipments = new LinkedList<>();
        this.patientPreferredEquipments = new LinkedList<>();
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
    public boolean hasDepartmentSpecialty() {
        return getDepartment().getSpecialtyToPriority().containsKey(specialty);
    }

    @JsonIgnore
    public int getSpecialtyPriority() {
        return getDepartment().getSpecialtyToPriority().get(specialty);
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

    public void addRequiredEquipment(String equipment) {
        if (!patientRequiredEquipments.contains(equipment)) {
            this.patientRequiredEquipments.add(equipment);
        }
    }

    public void addPreferredEquipment(String equipment) {
        if (!patientPreferredEquipments.contains(equipment)) {
            this.patientPreferredEquipments.add(equipment);
        }
    }

    @Override
    public String toString() {
        return patientName + "(" + arrivalDate + "-" + departureDate + ")";
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public void setPatientGender(Gender patientGender) {
        this.patientGender = patientGender;
    }

    public Gender getPatientGender() {
        return patientGender;
    }

    public void setPatientAge(int patientAge) {
        this.patientAge = patientAge;
    }

    public int getPatientAge() {
        return patientAge;
    }

    public Integer getPatientPreferredMaximumRoomCapacity() {
        return patientPreferredMaximumRoomCapacity;
    }

    public void setPatientPreferredMaximumRoomCapacity(Integer patientPreferredMaximumRoomCapacity) {
        this.patientPreferredMaximumRoomCapacity = patientPreferredMaximumRoomCapacity;
    }

    public List<String> getPatientRequiredEquipments() {
        return patientRequiredEquipments;
    }

    public void setPatientRequiredEquipments(List<String> patientRequiredEquipments) {
        this.patientRequiredEquipments = patientRequiredEquipments;
    }

    public List<String> getPatientPreferredEquipments() {
        return patientPreferredEquipments;
    }

    public void setPatientPreferredEquipments(List<String> patientPreferredEquipments) {
        this.patientPreferredEquipments = patientPreferredEquipments;
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

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public Bed getBed() {
        return bed;
    }

    public void setBed(Bed bed) {
        this.bed = bed;
    }
}
