package org.acme.employeescheduling.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

import ai.timefold.solver.core.api.domain.common.PlanningId;

public class Employee {
    @PlanningId
    private String name;
    private Set<String> skills;

    private Set<LocalDate> unavailableDates;
    private Set<LocalDate> undesiredDates;
    private Set<LocalDate> desiredDates;

    public Employee() {

    }

    public Employee(String name, Set<String> skills,
        Set<LocalDate> unavailableDates, Set<LocalDate> undesiredDates, Set<LocalDate> desiredDates) {
        this.name = name;
        this.skills = skills;
        this.unavailableDates = unavailableDates;
        this.undesiredDates = undesiredDates;
        this.desiredDates = desiredDates;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getSkills() {
        return skills;
    }

    public void setSkills(Set<String> skills) {
        this.skills = skills;
    }

    public Set<LocalDate> getUnavailableDates() {
        return unavailableDates;
    }

    public void setUnavailableDates(Set<LocalDate> unavailableDates) {
        this.unavailableDates = unavailableDates;
    }

    public Set<LocalDate> getUndesiredDates() {
        return undesiredDates;
    }

    public void setUndesiredDates(Set<LocalDate> undesiredDates) {
        this.undesiredDates = undesiredDates;
    }

    public Set<LocalDate> getDesiredDates() {
        return desiredDates;
    }

    public void setDesiredDates(Set<LocalDate> desiredDates) {
        this.desiredDates = desiredDates;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Employee employee)) {
            return false;
        }
        return Objects.equals(getName(), employee.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
