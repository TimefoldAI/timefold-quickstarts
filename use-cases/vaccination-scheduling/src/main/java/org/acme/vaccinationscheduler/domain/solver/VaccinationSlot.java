package org.acme.vaccinationscheduler.domain.solver;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.acme.vaccinationscheduler.domain.Appointment;
import org.acme.vaccinationscheduler.domain.VaccinationCenter;
import org.acme.vaccinationscheduler.domain.VaccineType;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;

/**
 * Only used by Timefold, not part of the input or output model.
 * Follows the bucket design pattern, this is a bucket of {@link Appointment} instances.
 */
public class VaccinationSlot {

    @PlanningId
    private String id;

    private VaccinationCenter vaccinationCenter;
    private LocalDate date;
    private LocalTime startTime;
    private VaccineType vaccineType;

    private List<Appointment> unscheduledAppointments;
    private int capacity;

    public VaccinationSlot(String id, VaccinationCenter vaccinationCenter,
                           LocalDateTime startDateTime, VaccineType vaccineType, List<Appointment> unscheduledAppointments, int capacity) {
        this.id = id;
        this.vaccinationCenter = vaccinationCenter;
        this.date = startDateTime.toLocalDate();
        this.startTime = startDateTime.toLocalTime();
        this.vaccineType = vaccineType;
        this.unscheduledAppointments = unscheduledAppointments;
        this.capacity = capacity;
    }

    /** For testing purposes only */
    public VaccinationSlot(String id, VaccinationCenter vaccinationCenter,
            LocalDateTime startDateTime, VaccineType vaccineType, int capacity) {
        this.id = id;
        this.vaccinationCenter = vaccinationCenter;
        this.date = startDateTime == null ? null : startDateTime.toLocalDate();
        this.startTime = startDateTime == null ? null : startDateTime.toLocalTime();
        this.vaccineType = vaccineType;
        unscheduledAppointments = null;
        this.capacity = capacity;
    }


    public LocalDateTime getStartDateTime() {
        return LocalDateTime.of(date, startTime);
    }

    @Override
    public String toString() {
        return vaccinationCenter + "@" + date + "_" + startTime + "/" + vaccineType;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public VaccinationCenter getVaccinationCenter() {
        return vaccinationCenter;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public VaccineType getVaccineType() {
        return vaccineType;
    }

    public List<Appointment> getUnscheduledAppointments() {
        return unscheduledAppointments;
    }

    public int getCapacity() {
        return capacity;
    }

}
