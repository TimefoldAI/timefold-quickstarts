package org.acme.employeescheduling.domain;

import java.time.LocalDate;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

public class Availability {

    @PlanningId
    String id;

    Employee employee;

    LocalDate date;

    AvailabilityType availabilityType;

    public Availability() {
    }

    public Availability(String id, Employee employee, LocalDate date, AvailabilityType availabilityType) {
        this.id = id;
        this.employee = employee;
        this.date = date;
        this.availabilityType = availabilityType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate localDate) {
        this.date = localDate;
    }

    public AvailabilityType getAvailabilityType() {
        return availabilityType;
    }

    public void setAvailabilityType(AvailabilityType availabilityType) {
        this.availabilityType = availabilityType;
    }

    @Override
    public String toString() {
        return availabilityType + "(" + employee + ", " + date + ")";
    }
}
