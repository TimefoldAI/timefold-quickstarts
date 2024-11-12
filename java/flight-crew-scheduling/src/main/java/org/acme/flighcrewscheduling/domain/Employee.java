package org.acme.flighcrewscheduling.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Employee.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Employee {

    @PlanningId
    private String id;
    private String name;
    private Airport homeAirport;

    private List<String> skills;
    private List<LocalDate> unavailableDays;

    public Employee() {
        this.unavailableDays = new ArrayList<>();
    }

    public Employee(String id) {
        this.id = id;
        this.unavailableDays = new ArrayList<>();
    }

    public Employee(String id, String name) {
        this.id = id;
        this.name = name;
        this.unavailableDays = new ArrayList<>();
    }

    public Employee(String id, String name, Airport homeAirport, List<String> skills) {
        this.id = id;
        this.name = name;
        this.homeAirport = homeAirport;
        this.skills = skills;
    }

    @JsonIgnore
    public boolean hasSkill(String skill) {
        return skills.contains(skill);
    }

    @JsonIgnore
    public boolean isAvailable(LocalDate fromDateInclusive, LocalDate toDateInclusive) {
        if (unavailableDays == null) {
            return true;
        }
        return fromDateInclusive
                .datesUntil(toDateInclusive.plusDays(1))
                .noneMatch(unavailableDays::contains);
    }

    @Override
    public String toString() {
        return name;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Airport getHomeAirport() {
        return homeAirport;
    }

    public void setHomeAirport(Airport homeAirport) {
        this.homeAirport = homeAirport;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<LocalDate> getUnavailableDays() {
        return unavailableDays;
    }

    public void setUnavailableDays(List<LocalDate> unavailableDays) {
        this.unavailableDays = unavailableDays;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Employee employee))
            return false;
        return Objects.equals(getId(), employee.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
