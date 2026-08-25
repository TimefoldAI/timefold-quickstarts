package org.acme.bedallocation.domain;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.entity.PlanningPin;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

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

    @PlanningPin
    private boolean pinned;

    @PlanningVariable(allowsUnassigned = true)
    private Bed bed;

    public Stay() {
    }

    public Stay(String id, String patientName, Gender patientGender, int patientAge,
            Integer patientPreferredMaximumRoomCapacity, List<String> patientRequiredEquipments,
            List<String> patientPreferredEquipments, LocalDate arrivalDate, LocalDate departureDate, String specialty,
            Bed bed, boolean pinned) {
        this.id = id;
        this.patientName = patientName;
        this.patientGender = patientGender;
        this.patientAge = patientAge;
        this.patientPreferredMaximumRoomCapacity = patientPreferredMaximumRoomCapacity;
        this.patientRequiredEquipments = patientRequiredEquipments;
        this.patientPreferredEquipments = patientPreferredEquipments;
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
        this.specialty = specialty;
        this.bed = bed;
        this.pinned = pinned;
    }

    public int getNightCount() {
        return (int) DAYS.between(arrivalDate, departureDate) + 1;
    }

    public int calculateSameNightCount(Stay other) {
        LocalDate maxArrivalDate = arrivalDate.isBefore(other.arrivalDate) ? other.arrivalDate : arrivalDate;
        LocalDate minDepartureDate = departureDate.isBefore(other.departureDate) ? departureDate : other.departureDate;
        return Math.max(0, (int) DAYS.between(maxArrivalDate, minDepartureDate) + 1);
    }

    public boolean hasDepartmentSpecialty() {
        return getDepartment().specialtyToPriority().containsKey(specialty);
    }

    public int getSpecialtyPriority() {
        return getDepartment().specialtyToPriority().get(specialty);
    }

    public Room getRoom() {
        if (bed == null) {
            return null;
        }
        return bed.room();
    }

    public int getRoomCapacity() {
        if (bed == null) {
            return Integer.MIN_VALUE;
        }
        return bed.room().capacity();
    }

    public Department getDepartment() {
        if (bed == null) {
            return null;
        }
        return bed.room().department();
    }

    public GenderLimitation getRoomGenderLimitation() {
        if (bed == null) {
            return null;
        }
        return bed.room().genderLimitation();
    }

    @Override
    public String toString() {
        return patientName + "(" + arrivalDate + "-" + departureDate + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Stay stay)) {
            return false;
        }
        return Objects.equals(id, stay.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public String getId() {
        return id;
    }

    public String getPatientName() {
        return patientName;
    }

    public Gender getPatientGender() {
        return patientGender;
    }

    public int getPatientAge() {
        return patientAge;
    }

    public Integer getPatientPreferredMaximumRoomCapacity() {
        return patientPreferredMaximumRoomCapacity;
    }

    public List<String> getPatientRequiredEquipments() {
        return patientRequiredEquipments;
    }

    public List<String> getPatientPreferredEquipments() {
        return patientPreferredEquipments;
    }

    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public String getSpecialty() {
        return specialty;
    }

    public Bed getBed() {
        return bed;
    }

    public void setBed(Bed bed) {
        this.bed = bed;
    }

    public boolean isPinned() {
        return pinned;
    }
}
