package org.acme.employeescheduling.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

@Entity
@PlanningEntity(pinningFilter = ShiftPinningFilter.class)
public class Shift {
    @Id
    @PlanningId
    @GeneratedValue
    Long id;

    LocalDateTime start;
    @Column(name = "endDateTime") // "end" clashes with H2 syntax.
    LocalDateTime end;

    String location;
    String requiredSkill;

    @PlanningVariable
    @ManyToOne
    Employee employee;

    public Shift() {
    }

    public Shift(LocalDateTime start, LocalDateTime end, String location, String requiredSkill) {
        this(start, end, location, requiredSkill, null);
    }

    public Shift(LocalDateTime start, LocalDateTime end, String location, String requiredSkill, Employee employee) {
        this(null, start, end, location, requiredSkill, employee);
    }

    public Shift(Long id, LocalDateTime start, LocalDateTime end, String location, String requiredSkill, Employee employee) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.location = location;
        this.requiredSkill = requiredSkill;
        this.employee = employee;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRequiredSkill() {
        return requiredSkill;
    }

    public void setRequiredSkill(String requiredSkill) {
        this.requiredSkill = requiredSkill;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    @Override
    public String toString() {
        return location + " " + start + "-" + end;
    }
}
