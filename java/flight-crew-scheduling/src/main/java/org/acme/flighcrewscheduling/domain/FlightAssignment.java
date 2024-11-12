package org.acme.flighcrewscheduling.domain;

import java.time.LocalDateTime;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

import com.fasterxml.jackson.annotation.JsonIgnore;

@PlanningEntity
public class FlightAssignment {

    @PlanningId
    private String id;
    private Flight flight;
    private int indexInFlight;
    private String requiredSkill;

    @PlanningVariable
    private Employee employee;

    public FlightAssignment() {
    }

    public FlightAssignment(String id, Flight flight) {
        this.id = id;
        this.flight = flight;
    }

    public FlightAssignment(String id, Flight flight, int indexInFlight, String requiredSkill) {
        this.id = id;
        this.flight = flight;
        this.indexInFlight = indexInFlight;
        this.requiredSkill = requiredSkill;
    }

    @JsonIgnore
    public boolean hasRequiredSkills() {
        return getEmployee().hasSkill(requiredSkill);
    }

    @JsonIgnore
    public boolean isUnavailableEmployee() {
        return !getEmployee().isAvailable(getFlight().getDepartureUTCDate(), getFlight().getArrivalUTCDateTime().toLocalDate());
    }

    @JsonIgnore
    public LocalDateTime getDepartureUTCDateTime() {
        return flight.getDepartureUTCDateTime();
    }

    @Override
    public String toString() {
        return flight + "-" + indexInFlight;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public Flight getFlight() {
        return flight;
    }

    public void setFlight(Flight flight) {
        this.flight = flight;
    }

    public int getIndexInFlight() {
        return indexInFlight;
    }

    public void setIndexInFlight(int indexInFlight) {
        this.indexInFlight = indexInFlight;
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
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof FlightAssignment that))
            return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
